(ns otaservice.core
  (:require [otaservice.security :as sec]
            [otaservice.api :refer [rest-api]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]))

(def app
  (-> rest-api
      (wrap-authorization sec/backend)
      (wrap-authentication sec/backend)))
