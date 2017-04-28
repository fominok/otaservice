(ns deploy
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [environ.core :refer [env]]))

(def pg-db (env :database-url))

(defn load-config []
  {:datastore (jdbc/sql-database pg-db)
   :migrations (jdbc/load-resources "migrations")})

(defn migrate []
  (repl/migrate (load-config)))

(defn rollback []
  (repl/rollback (load-config)))
