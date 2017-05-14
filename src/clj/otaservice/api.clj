(ns otaservice.api
  (:require [otaservice.security :as sec]
            [otaservice.handlers :as handlers]
            [compojure.api.sweet :as sw]
            [compojure.api.upload :as up]
            [ring.util.http-response :as rh]
            [schema.core :as s])
  (:import java.io.File))

;;; Schemas

(s/defschema Credentials
  {:username s/Str
   :password s/Str})

(s/defschema UsError
  {:error s/Str})

(s/defschema Device
  {:mac s/Str
   :developer s/Str
   (s/optional-key :visual_name) s/Str
   (s/optional-key :visual_icon) s/Str
   :last_active java.sql.Timestamp
   :device_version s/Str
   (s/optional-key :service_version) s/Str})

(s/defschema DeviceCrop
  {(s/optional-key :visual_name) s/Str
   (s/optional-key :visual_icon) s/Str})

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

               ;; Authentication things

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
                                    400 {:schema {:error {(s/optional-key :username) [s/Str]
                                                          (s/optional-key :password) [s/Str]}}
                                         :description "Validation failed"}}
                        :return {:identity s/Str}
                        :summary "Register new user"
                        (handlers/register-user creds))

               ;; Developer's private zone

               (sw/GET "/:user/devices" []
                       :return [Device]
                       :path-params [user :- s/Str]
                       :auth-rules sec/owner-only
                       :summary "Get devices list for user"
                       (handlers/get-devices user))

               (sw/GET "/:user/devices/:mac" []
                       :return Device
                       :path-params [user :- s/Str mac :- s/Str]
                       :auth-rules sec/owner-only
                       :summary "Get device info by mac"
                       (handlers/get-one-device mac))

               (sw/POST "/:user/devices/:mac" []
                        :return Device
                        :path-params [user :- s/Str mac :- s/Str]
                        :auth-rules sec/owner-only
                        :body [body DeviceCrop]
                        :summary "Update device info by mac"
                        (handlers/update-device-info user mac body))

               (sw/POST "/:user/devices/:mac/upload" []
                        :path-params [user :- s/Str mac :- s/Str]
                        :multipart-params [firmware :- up/TempFileUpload version :- s/Str]
                        :middleware [up/wrap-multipart-params]
                        :auth-rules sec/owner-only
                        :summary "Upload new firmware for device"
                        (handlers/upload-firmware user mac firmware version))

               ;; ESP8266 api endpoint
               (sw/GET "/:user/ping" request
                       :path-params [user :- s/Str]
                       :query-params [version :- s/Str]
                       :responses {304 {:description "No updates"}
                                   422 {:schema s/Str :description "Wrong user-agent"}}
                       :header-params [user-agent :- s/Str x-esp8266-sta-mac :- s/Str]
                       :return File
                       :summary "ESP8266 endpoint to ping server for updates"
                       (handlers/esp-ping user user-agent x-esp8266-sta-mac version)))))
