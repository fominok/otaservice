(ns otaservice.handlers
  (:require [otaservice.security :as sec]
            [ring.util.response :as r]
            [ring.util.http-response :as rh]
            [clojure.java.io :as io]
            [otaservice.db.queries :as q]
            [otaservice.db :as db]
            [buddy.sign.jwt :as jwt]))

(defn- find-user [id pass]
  (if (and (= id "yolo") (= pass "swag"))
    {:id "yolo"}
    nil))

(defn login-handler [request]
  (if-let [user (find-user (:username request)
                           (:password request))]
    (rh/ok {:token (jwt/sign {:user (:id user)} sec/secret)})
    (rh/unauthorized {:reason "Wrong credentials"})))

(defn ota-update
  "Performs OTA update for ESP8266"
  [request]
  (let [bin (io/file "resources/public/webserver.bin")]
    (-> (r/response (io/input-stream bin))
        (r/header "Content-Type" "application/octet-stream")
        (r/header "Content-Disposition" "attachment")
        (r/header "Content-Length" (.length bin))
        (r/status 200))))


(defn user-exists-handler [id]
  (->
   (q/find-user db/database-uri {:identity id})
   empty? not
   rh/ok))
