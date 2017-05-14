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
         (let [register-req #(-> (mock/request :post "/api/v1/register")
                                 (mock/content-type "application/json")
                                 (mock/body (cheshire/generate-string %)))]
           (fact "if register with short login/pass proper error returned"
                 (:status (rest-api (register-req {:username "tes" :password "pass"}))) => 400))))
