(defproject reactrails-in-reagent "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

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

                 [figwheel-sidecar "0.5.0"]

                 ]
  :plugins [[lein-cljsbuild "1.1.1"]
            ;[lein-figwheel "0.5.0-1"]
            ]

  :profiles {:dev {:dependencies [[reloaded.repl "0.2.1"]
                                  [ring/ring-devel "1.4.0"]
                                  [ring/ring-mock "0.3.0"]
                                  [peridot "0.4.2"]
                                  [juxt/iota "0.2.0"]

                                  [compojure "1.0.2"]]}}

  :source-paths ["src/clj"
                 "src/cljs"
                 "src/dev"
                 "src/cljc"
                 "script"]

  :test-paths ["test"]



  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs" "src/cljc"]

                :figwheel {:on-jsload "reactrails-in-reagent.core/on-js-reload"}

                :compiler {:main reactrails-in-reagent.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/main.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/main.js"
                           :main reactrails-in-reagent.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             }
  )