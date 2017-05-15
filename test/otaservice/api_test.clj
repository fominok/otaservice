(ns otaservice.api-test
  (:use midje.sweet)
  (:require [ring.mock.request :as mock]
            [cheshire.core :as cheshire]
            [deploy :refer :all]
            [otaservice.api :refer [rest-api]]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(with-state-changes [(before :facts (migrate))
                     (after :facts (rollback-all))]
  (facts "about authentication"
         (let [register-req #(rest-api (-> (mock/request :post "/api/v1/register")
                                           (mock/content-type "application/json")
                                           (mock/body (cheshire/generate-string %))))]

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
                   (:error body) => "User already exists")))))
