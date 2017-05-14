(ns otaservice.validators
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]))

(defn- too-short
  "Returns false if length of x is less than min-len"
  [min-len x]
  (->> x count (<= min-len)))

(defn check-creds-input
  "Function to validate Registration fields (Credentials model)"
  [creds]
  (b/validate creds
              :username [v/required [(partial too-short 4) :message "Username length must be greater than 3"]]
              :password [v/required [(partial too-short 8) :message "Password length must be greater than 7"]]))
