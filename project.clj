(defproject otaservice "0.0.1"
  :description "Web-service to store ESP8266 firmwares and to distribute updates"
  :url "http://github.com/fominok/otaservice"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [metosin/compojure-api "1.2.0-alpha5"]
                 [buddy/buddy-auth "1.4.1"]
                 [buddy/buddy-hashers "1.2.0"]
                 [environ "1.1.0"]
                 [ragtime "0.7.1"]
                 [com.layerware/hugsql "0.4.7"]
                 [bouncer "1.0.1"]
                 [midje "1.8.3"]
                 [postgresql/postgresql "9.3-1102.jdbc41"]]
  :source-paths ["src/cljc" "src/clj"]
  :plugins [[lein-ring "0.9.7"]
            [lein-environ "1.1.0"]
            [lein-midje "3.2.1"]]
  :uberjar-name "otaservice-standalone.jar"
  :ring {:handler otaservice.core/app
         :init deploy/migrate
         :nrepl {:start? true
                 :port 1337}}
  :aliases {"migrate" ["run" "-m" "deploy/migrate"]
            "rollback" ["run" "-m" "deploy/rollback"]
            "uberjar" ["ring" "uberjar"]}
  :profiles {:dev {:resource-paths ["config/dev"]
                   :dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring/ring-mock "0.3.0"]
                                  [cheshire "5.7.1"]]}
             :midje {:env {:database-url "jdbc:postgresql://localhost/otatest"
                           :secret "supersecret"}}
             :uberjar {:aot :all}})
