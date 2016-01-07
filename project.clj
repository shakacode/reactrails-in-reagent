(defproject reactrails-in-reagent "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.0.0"

  :jvm-opts ^:replace ["-Xms512m" "-Xmx512m" "-server"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"]
                 [reagent "0.5.1"]
                 [reagent-forms "0.5.13"]
                 [com.datomic/datomic-free "0.9.5344"]
                 [bidi "1.23.1"]
                 [org.immutant/web "2.1.1"]
                 [liberator "0.13"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [com.rpl/specter "0.9.0"]
                 [prismatic/schema "1.0.4"]
                 [com.stuartsierra/component "0.2.3"]
                 [cheshire "5.5.0"]
                 [cljs-ajax "0.5.2"]
                 [environ "1.0.1"]]

  :plugins [[lein-cljsbuild "1.1.1"]]

  :source-paths ["src/clj" "src/cljc"]

  :test-paths ["test"]

  :uberjar-name "reactrails-in-reagent-standalone.jar"

  :main reactrails-in-reagent.core

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "resources/public/js/main.js"
                                    "target"]

  :profiles {:dev {:dependencies [[reloaded.repl "0.2.1"]
                                  [ring/ring-devel "1.4.0"]
                                  [ring/ring-mock "0.3.0"]
                                  [peridot "0.4.2"]
                                  [juxt/iota "0.2.0"]
                                  [figwheel-sidecar "0.5.0-1"]

                                  [compojure "1.0.2"]]

                   :source-paths ["src/dev"
                                  "script"]}

             :uberjar {:aot :all
                       :hooks [leiningen.cljsbuild]}

             :production {:env {:production true}}}

  :cljsbuild {:builds
              [;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src/cljs" "src/cljc"]
                :compiler {:output-to "resources/public/js/main.js"
                           :main reactrails-in-reagent.core
                           :optimizations :advanced
                           :pretty-print false}}

               {:id "dev"
                :source-paths ["src/cljs" "src/cljc"]

                :figwheel {:on-jsload "reactrails-in-reagent.core/on-js-reload"}

                :compiler {:main reactrails-in-reagent.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/main.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}]}

  :figwheel {:css-dirs ["resources/public/css"]})