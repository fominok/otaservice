(ns otaservice.handlers
  (:require [otaservice.security :as sec]
            [otaservice.validators :as v]
            [otaservice.tools :as t]
            [ring.util.response :as r]
            [ring.util.http-response :as rh]
            [clojure.java.io :as io]
            [otaservice.db.queries :as q]
            [otaservice.db :as db]
            [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]))

(defn- find-user [{:keys [username password]}]
  (if-let [{:keys [identity pass]} (not-empty (q/find-user
                                               db/database-uri
                                               {:identity username}))]
    (when (hashers/check password pass)
      username)
    nil))

(defn login-handler [creds]
  (if-let [user (find-user creds)]
    (rh/ok {:token (jwt/sign {:user user} sec/secret)})
    (rh/unauthorized {:error "Wrong credentials"})))

(defn esp-ping
  [user user-agent mac version]
  (if (not= user-agent "ESP8266-http-Update")
    (rh/unprocessable-entity "ESP8266 only!")
    (do
      (println user user-agent mac version)
      (try
        (q/device-ping! db/database-uri {:mac (clojure.string/replace mac #":" "")
                                         :developer user
                                         :last-active (t/now)
                                         :device-version version})
        (catch java.sql.BatchUpdateException e nil)) ;; TODO: log
      (rh/not-modified)))
  #_(let [bin (io/file "resources/public/webserver.bin")]
    (-> (r/response (io/input-stream bin))
        (r/header "Content-Type" "application/octet-stream")
        (r/header "Content-Disposition" "attachment")
        (r/header "Content-Length" (.length bin))
        (r/status 200))))

(defn- find-user-by-id [id]
  (-> (q/find-user db/database-uri {:identity id})
      empty? not))

(defn user-exists-handler [id]
  (rh/ok (find-user-by-id id)))

(defn register-user [{:keys [username password] :as creds}]
  (if-let [validation-errors (first (v/check-creds-input creds))]
    (rh/bad-request {:error validation-errors})

    (if (find-user-by-id username)
      (rh/conflict {:error "User already exists"})
      (do
        (q/create-user! db/database-uri {:identity username
                                        :pass-hashed (hashers/derive password)})
        (rh/ok {:identity username})))))
