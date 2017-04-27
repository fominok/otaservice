(defproject otaservice "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [metosin/compojure-api "1.2.0-alpha5"]
                 [buddy/buddy-auth "1.4.1"]
                 [environ "1.1.0"]
                 [ragtime "0.7.1"]
                 [com.layerware/hugsql "0.4.7"]
                 [postgresql/postgresql "9.3-1102.jdbc41"]]
  :source-paths ["src"]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler otaservice.core/app
         :nrepl {:start? true
                 :port 1337}}
  :aliases {"migrate" ["run" "-m" "deploy/migrate"]
            "rollback" ["run" "-m" "deploy/rollback"]}
  :profiles {:dev {:resource-paths ["config/dev"]
                   :dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.0"]]}
             :uberjar {:aot :all}})
