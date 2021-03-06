(ns otaservice.db
  (:require [environ.core :refer [env]]))

(def database-uri (env :database-url))

(defn restruct [m]
  {:keys (map name (keys m))
   :values (vals m)})
