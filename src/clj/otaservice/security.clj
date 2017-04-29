(ns otaservice.security
  (:require [otaservice.tools :as t]
           [ring.util.http-response :as rh]
           [compojure.api.meta :refer [restructure-param]]
           [buddy.auth.accessrules :refer [wrap-access-rules]]
           [buddy.auth.backends :as backends]))

;;; Access rules

(defn authenticated-user
  "Check if there is any identity with request"
  [request]
  (t/not-nil? (:identity request)))

;;; Rules Compojure-api mod

(defn access-error
  [req val]
  (rh/forbidden {:error "No access rights to this resource"}))

(defn wrap-rule [handler rule]
  (-> handler
      (wrap-access-rules {:rules [{:pattern #".*"
                                   :handler rule}]
                          :on-error access-error})))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-rule rule]))

;;; JWS Token backend

(def secret "lolsecretjk")

(def backend (backends/jws {:secret secret}))
