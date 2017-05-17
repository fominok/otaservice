(defproject otaservice "0.4.20"
  :description "Web-service to store ESP8266 firmwares and to distribute updates"
  :url "http://github.com/fominok/otaservice"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.542" :exclusions [org.apache.ant/ant]]
                 [compojure "1.5.1"]
                 [re-frame "0.9.3"]
                 [reagent "0.6.1"]
                 [cljs-http "0.1.43"]
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
            [lein-cljsbuild "1.1.6"]
            ;; [lein-figwheel "0.5.10"]
            [lein-midje "3.2.1"]]

  ;;TODO add mount + figwheel
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:main "otaservice.app.core"
                                   :output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :pretty-print true}}]}
  :hooks [leiningen.cljsbuild]
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
