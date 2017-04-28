(ns otaservice.db.queries
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "otaservice/db/sql/queries.sql")
