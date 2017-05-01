(ns otaservice.tools)

(def not-nil? (complement nil?))

(defn now [] (java.sql.Timestamp. (.getTime (java.util.Date.))))
