(ns otaservice.api-test
  (:use midje.sweet)
  (:require [ring.mock.request :as mock]
            [cheshire.core :as cheshire]
            [deploy :refer :all]
            [buddy.sign.jwt :as jwt]
            [environ.core :refer [env]]
            [otaservice.core :refer [app]]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(defn register-req [query]
  (app (-> (mock/request :post "/api/v1/register")
           (mock/content-type "application/json")
           (mock/body (cheshire/generate-string query)))))

(defn login-req [query]
  (app (-> (mock/request :post "/api/v1/login")
           (mock/content-type "application/json")
           (mock/body (cheshire/generate-string query)))))

(with-state-changes [(before :facts (migrate))
                     (after :facts (rollback-all))]

  (facts "about registration"
         (fact "if register with short login/pass proper error returned"
               (let [response (register-req {:username "tes" :password "passwor"})
                     body (parse-body (:body response))]
                 (:status response) => 400
                 (:error body) => {:username ["Username length must be greater than 3"]
                                   :password ["Password length must be greater than 7"]}))

         (fact "correctly filled field do not show up in error message"
               (let [response (register-req {:username "user" :password "wrong"})
                     body (parse-body (:body response))]
                 (:status response) => 400
                 (:error body) =not=> (contains {:username anything})
                 (:error body) => (contains {:password anything})))

         (fact "successful registration returns username"
               (let [sameuser "user"
                     response (register-req {:username sameuser :password "password"})
                     body (parse-body (:body response))]
                 (:status response) => 200
                 (:identity body) => sameuser))

         (fact "registering with existing username returns proper code and error"
               (let [sameuser "user"
                     _ (register-req {:username sameuser :password "password"})
                     response (register-req {:username sameuser :password "anything"})
                     body (parse-body (:body response))]
                 (:status response) => 409
                 (:error body) => "User already exists"))))

(let [creds {:username "user" :password "password"}
      inv-pass {:username "user" :password "wrong"}]
  (with-state-changes [(before :contents (do (migrate)
                                             (register-req creds)))
                       (after :contents (rollback-all))]

    (facts "about authentication"
           (fact "cannot login with wrong password"
                 (let [response (login-req inv-pass)
                       body (parse-body (:body response))]
                   (:status response) => 401
                   (:error body) => "Wrong credentials"))
           (fact "successful login returns JWS token with encrypted identity"
                 (let [response (login-req creds)
                       body (parse-body (:body response))]
                   (:status response) => 200
                   (jwt/unsign (:token body) (env :secret)) => {:user (:username creds)})))))


(let [creds {:username "user" :password "password"}
      other-creds {:username "otheruser" :password "anypassword"}
      non-user {:username "nobody" :password "password"}
      version "4.20"
      mac "F9:EC:6C:C0:29:25"]
  (with-state-changes [(before :contents (do (migrate)
                                             (register-req creds)
                                             (register-req other-creds)))
                       (after :contents (rollback-all))]
    (let [ping-req #(app (-> (mock/request :get (str "/api/v1/" (:username %) "/ping"))
                             (mock/header "user-agent" "ESP8266-http-Update")
                             (mock/header "x-esp8266-sta-mac" mac)
                             (mock/query-string {:version version})))
          nouser-ping-resp (ping-req non-user)
          exst-ping-resp (ping-req creds)
          token (-> (login-req creds)
                    :body parse-body :token)
          other-token (-> (login-req other-creds)
                          :body parse-body :token)
          auth-header #(mock/header % "Authorization" (str "Token " token))
          other-auth-header #(mock/header % "Authorization" (str "Token " other-token))]

      (facts "about device management"

             (fact "no user equals no firmware for existing user and returning 304"
                   (:status nouser-ping-resp) => 304
                   (:status exst-ping-resp) => 304)

             (fact "device management requires authentication"
                   (let [resp (app (-> (mock/request :get
                                                     (str "/api/v1/" (:username creds) "/devices"))))
                         body (parse-body (:body resp))]
                     (:status resp) => 403
                     (:error body) => "No access rights to this resource"))
             (fact "user can access his devices only"
                   (let [resp-correct (app (-> (mock/request :get (str "/api/v1/" (:username creds) "/devices"))
                                               auth-header))
                         resp-wrong (app (-> (mock/request :get (str "/api/v1/" (:username creds) "/devices"))
                                               other-auth-header))] ;; note token for other user
                     (:status resp-correct) => 200
                     (:status resp-wrong) => 403))))))
