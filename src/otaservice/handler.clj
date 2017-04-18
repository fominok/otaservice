(ns otaservice.handler
  (:require #_[compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :as r]
            [ring.util.http-response :as rh]
            [clojure.java.io :as io]
            [compojure.api.sweet :as sw]
            [schema.core :as s]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn ota-update
  "Performs OTA update for ESP8266"
  [request]
  (let [bin (io/file "resources/public/webserver.bin")]
    (-> (r/response (io/input-stream bin))
        (r/header "Content-Type" "application/octet-stream")
        (r/header "Content-Disposition" "attachment")
        (r/header "Content-Length" (.length bin))
        (r/status 200))))

(def rest-api
  (sw/api
   {:swagger {:ui "/"
              :spec "/swagger.json"
              :data {:info {:title "OTA Service"
                            :description "ESP8266 OTA Updates service"}
                     :tags [{:name "api1" :description "first generation api"}]}}}
   (sw/context "/api/v1" []
            :tags ["api1"]

            (sw/GET "/plus" []
                 :return Long
                 :query-params [x :- Long, y :- Long]
                 :summary "Adds two numbers together"
                 (rh/ok (+ x y)))
            
            (sw/GET "/update" request
                 :summary "Performs OTA update for ESP8266"
                 (ota-update request)))))

#_(defroutes app-routes
    (GET "/" [] "Hello World")
    (GET "/update" request (ota-update request))
    (route/not-found "Not Found")) 

(def app
    (wrap-defaults rest-api site-defaults))
