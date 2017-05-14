(ns deploy
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [ragtime.core :as ragtime]
            [otaservice.db :as db]))

;; Functions for starting migrations

(defn load-config []
  {:datastore (jdbc/sql-database db/database-uri)
   :migrations (jdbc/load-resources "migrations")})

(defn migrate []
  (repl/migrate (load-config)))

(defn rollback []
  (repl/rollback (load-config)))

(defn rollback-all []
  (let [conf (load-config)
        n (count (:migrations conf))]
    (repl/rollback conf n)))
