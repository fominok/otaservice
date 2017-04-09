(ns otaservice.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as r]
            [clojure.java.io :as io]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn ota-update
  "Performs OTA update for ESP8266"
  [request]
  (println request)
  (let [bin (io/file "resources/public/webserver.bin")]
    (-> (r/response (io/input-stream bin))
        (r/header "Content-Type" "application/octet-stream")
        (r/header "Content-Disposition" "attachment")
        (r/header "Content-Length" (.length bin))
        (r/status 200))))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/update" request (ota-update request))
  (route/not-found "Not Found")) 

(def app
  (wrap-defaults app-routes site-defaults)) 
