(ns otaservice.db
  (:require [environ.core :refer [env]]))

(def database-uri (env :database-url))
