(ns otaservice.api
  (:require [otaservice.security :as sec]
            [otaservice.handlers :as handlers]
            [compojure.api.sweet :as sw]
            [ring.util.http-response :as rh]
            [schema.core :as s])
  (:import java.io.File))

;;; Schemas

(s/defschema Credentials
  {:username s/Str
   :password s/Str})

(s/defschema UsError
  {:reason s/Str})

;;; Api definitions

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
                        :responses {401 {:schema UsError
                                         :description "No user with this login & pass found."}}
                        :return {:token s/Str}
                        :summary "Authorize and receive jws token"
                        (handlers/login-handler creds))

               (sw/POST "/register" []
                        :body [creds Credentials]
                        :responses {409 {:schema UsError
                                         :description "User exists already"}
                                    400 {:schema {:errors {(s/optional-key :username) [s/Str]
                                                           (s/optional-key :password) [s/Str]}}
                                         :description "Validation failed"}}
                        :return {:identity s/Str}
                        :summary "Register new user"
                        (handlers/register-user creds))

               (sw/GET "/plus" []
                       :return Long
                       :query-params [x :- Long, y :- Long]
                       :auth-rules sec/authenticated-user
                       :summary "Adds two numbers together"
                       (rh/ok (+ x y)))

               (sw/GET "/user_exists" []
                       :return s/Bool
                       :query-params [id :- s/Str]
                       :summary "Check if user exists"
                       (handlers/user-exists-handler id))

               (sw/GET "/update" request
                       :summary "Performs OTA update for ESP8266"
                       :return File
                       (handlers/ota-update request)))))