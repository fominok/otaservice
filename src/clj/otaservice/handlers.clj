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
            [buddy.sign.jwt :as jwt]
            [clojure.java.io :as io]))

(defn- find-user [{:keys [username password]}]
  (if-let [{:keys [identity pass]} (not-empty (q/find-user
                                               db/database-uri
                                               {:identity username}))]
    (when (hashers/check password pass)
      username)
    nil))

(defn login-handler [creds]
  (if-let [user (find-user creds)]
    (rh/ok {:token (jwt/sign {:user user} sec/secret)
            :identity user}) ;; TODO: add expiration date to signed jwt
    (rh/unauthorized {:error "Wrong credentials"})))

(defn- ping-device! [user mac version]
  (try
    (q/device-ping! db/database-uri
                    {:mac mac
                     :developer user
                     :last-active (t/now)
                     :device-version version})
    (q/device-info db/database-uri {:mac mac})
    (catch java.sql.BatchUpdateException e nil)))  ;; TODO: log

(defn- update-response [device]
  (let [mac (:mac device)
        user (clojure.string/trim (:developer device))
        version (:service_version device)
        filename (str "resources/" user "/" mac "_" version)
        bin (io/file filename)]
    (-> (r/response (io/input-stream bin))
        (r/header "Content-Type" "application/octet-stream")
        (r/header "Content-Disposition" "attachment")
        (r/header "Content-Length" (.length bin))
        (r/status 200))))

(defn esp-ping
  [user user-agent full-mac version]
  (if (not= user-agent "ESP8266-http-Update")
    (rh/unprocessable-entity "ESP8266 only!")
    (if-let [device (ping-device! user (clojure.string/replace full-mac #":" "") version)]
      (if (and (t/not-nil? (:service_version device)) (not= (:service_version device) version))
        (update-response device)
        (rh/not-modified)) ;; no updates
      (rh/not-modified)))) ;; no developer registered with this name

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

(defn upload-firmware [user mac firmware version]
  (let [filename (str "resources/" user "/" mac "_" version)]
    (io/make-parents filename)
    (io/copy (:tempfile firmware) (io/file filename))
    (q/update-device! db/database-uri (-> {:service_version version}
                                          db/restruct
                                          (assoc :mac mac)))
    (rh/ok version)))

(defn get-devices [user]
  (rh/ok (map
          (comp #(update % :developer clojure.string/trim) t/filter-nil-map)
          (q/devices-by-user db/database-uri {:user user}))))

(defn get-one-device [mac]
  (-> (q/device-info db/database-uri {:mac mac})
      (update :developer clojure.string/trim)
      t/filter-nil-map
      rh/ok))

(defn update-device-info [user mac body]
  (q/update-device! db/database-uri (-> body
                                        db/restruct
                                        (assoc :mac mac)))
  (get-one-device mac))
