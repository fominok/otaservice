(ns otaservice.core
  (:require [otaservice.security :as sec]
            [otaservice.api :refer [rest-api]]
            [compojure.core :refer [GET defroutes routes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [resource-response]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]))

(defroutes non-api
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/login" [] (resource-response "login.html" {:root "public"}))
  (resources "/"))

(def app
  (routes non-api (-> rest-api
                      (wrap-authorization sec/backend)
                      (wrap-authentication sec/backend))))
