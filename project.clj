(do (require '[clojure.edn :as edn])
    (def cljs-test-build
      {:id           "test"
       :source-paths ["src/cljs" "src/cljc" "test/frontend"]
       :compiler     {:main          "test.runner"
                      :output-to     "resources/public/js/compiled/test.js"
                      :output-dir    "resources/public/js/compiled/test_out"
                      :optimizations :none}})
    (def cljs-test-min
      {:id           "test-min"
       :source-paths ["src/cljs" "src/cljc" "test/frontend"]
       :compiler     {:main          "test.runner"
                      :output-to     "resources/public/js/compiled/test-min.js"
                      :pretty-print false
                      :optimizations :advanced}})
    (def cljs-dev-builds (-> "figwheel.edn"
                           slurp
                           edn/read-string
                           :builds
                           (conj cljs-test-build cljs-test-min))))


(defproject reactrails-in-reagent "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.0.0"

  :jvm-opts ^:replace ["-Xms512m" "-Xmx512m" "-server"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"]
                 [reagent "0.5.1" :exclusions [cljsjs/react]]
                 [cljsjs/react "0.14.0-1"]
                 [reagent-forms "0.5.13"]
                 [com.datomic/datomic-free "0.9.5344"]
                 [bidi "1.23.1"]
                 [org.immutant/web "2.1.1"]
                 [liberator "0.13"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [com.rpl/specter "0.9.0"]
                 [prismatic/schema "1.0.4"]
                 [com.stuartsierra/component "0.3.1"]
                 [cheshire "5.5.0"]
                 [cljs-ajax "0.5.2"]
                 [environ "1.0.1"]]

  :plugins [[lein-cljsbuild "1.1.1" :exclusions [org.clojure/clojure]]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "resources/public/js/main.js"
                                    "target"]

  :source-paths ["src/clj" "src/cljc"]

  :profiles
  {:cljs-dev   {:cljsbuild {:builds ~cljs-dev-builds}}
   :cljs-prod  {:cljsbuild {:builds [{:id           "min"
                                      :source-paths ["src/cljs" "src/cljc"]
                                      :compiler     {:main          reactrails-in-reagent.core
                                                     :output-to     "resources/public/js/main.js"
                                                     :optimizations :advanced
                                                     :pretty-print  false}}]}}
   :dev        [:cljs-dev
                :cljs-prod
                {:dependencies   [[reloaded.repl "0.2.1"]
                                  [ring/ring-devel "1.4.0"]
                                  [ring/ring-mock "0.3.0"]
                                  [peridot "0.4.2"]
                                  [juxt/iota "0.2.0"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]
                                  [figwheel-sidecar "0.5.0-3" :exclusions [com.stuartsierra/component]]
                                  [devcards "0.2.1"]
                                  [karma-reporter "1.0.0"]]
                 :repl-options   {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                 :plugins        [[lein-gorilla "0.3.5"]
                                  [lein-figwheel "0.5.0-3"]
                                  [lein-pprint "1.1.2"]]
                 :source-paths   ["src/dev"]
                 :test-paths     ["test/backend"]
                 :resource-paths ["dev-resources"]}]

   :uberjar    [:cljs-prod
                {:uberjar-name "reactrails-in-reagent-standalone.jar"
                 :main         reactrails-in-reagent.core
                 :aot          :all
                 :hooks        [leiningen.cljsbuild]}]

   :production {:env {:production true}}}

  :figwheel {:css-dirs ["resources/public/css"]}
  )
