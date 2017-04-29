(ns deploy
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [otaservice.db :as db]))

(defn load-config []
  {:datastore (jdbc/sql-database db/database-uri)
   :migrations (jdbc/load-resources "migrations")})

(defn migrate []
  (repl/migrate (load-config)))

(defn rollback []
  (repl/rollback (load-config)))
