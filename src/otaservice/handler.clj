(ns otaservice.handler
  (:require [compojure.route :as route]
            [ring.util.response :as r]
            [ring.util.http-response :as rh]
            [clojure.java.io :as io]
            [compojure.api.sweet :as sw]
            [compojure.api.meta :refer [restructure-param]]
            [schema.core :as s]
            [buddy.auth.accessrules :refer [wrap-access-rules]]
            [buddy.auth.backends :as backends]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.sign.jwt :as jwt]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [otaservice.tools :as t])
  (:import (java.io File)))

(defn authenticated-user
  "Check if there is any identity with request"
  [request]
  (t/not-nil? (:identity request)))

(defn access-error
  [req val]
  (rh/forbidden {:reason "No access rights to this resource"}))

(defn wrap-rule [handler rule]
  (-> handler
      (wrap-access-rules {:rules [{:pattern #".*"
                                   :handler rule}]
                          :on-error access-error})))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-rule rule]))

(def secret "lolsecretjk")
(def backend (backends/jws {:secret secret}))

(s/defschema Credentials
  {:username s/Str
   :password s/Str})

(defn find-user [id pass]
  (if (and (= id "yolo") (= pass "swag"))
    {:id "yolo"}
    nil))

(defn login-handler [request]
  (if-let [user (find-user (:username request)
                           (:password request))]
    (rh/ok {:token (jwt/sign {:user (:id user)} secret)})
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

(def rest-api
  (sw/api
   {:swagger {:ui "/api"
              :spec "/swagger.json"
              :data {:info {:title "OTA Service"
                            :description "ESP8266 OTA Updates service"}
                     :securityDefinitions {:api_key {:type "apiKey"
                                                     :name "Authorization"
                                                     :in "header"}}
                     :tags [{:name "api1" :description "first generation api"}]}}}
   (sw/context "/api/v1" []
               :tags ["api1"]

               (sw/POST "/login" []
                        :body [creds Credentials]
                        :responses {401 {:schema {:reason s/Str}
                                         :description "No user with this login & pass found."}}
                        :return {:token s/Str}
                        :summary "authorize and receive jwt token"
                        (login-handler creds))

               (sw/GET "/plus" []
                       :return Long
                       :query-params [x :- Long, y :- Long]
                       :auth-rules authenticated-user
                       :summary "Adds two numbers together"
                       (rh/ok (+ x y)))
               
               (sw/GET "/update" request
                       :summary "Performs OTA update for ESP8266"
                       :return File
                       (ota-update request)))))

(def app
  (-> rest-api
      (wrap-authorization backend)
      (wrap-authentication backend)))
