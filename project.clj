(defproject otaservice "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [metosin/compojure-api "1.2.0-alpha5"]
                 [buddy/buddy-auth "1.4.1"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler otaservice.core/app
         :nrepl {:start? true
                 :port 1337}}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
