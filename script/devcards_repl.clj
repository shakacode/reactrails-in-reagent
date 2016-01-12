(use 'figwheel-sidecar.repl-api)
(start-figwheel! {:figwheel-options {:css-dirs ["resources/public/css"]}
                  :build-ids ["devcards"]
                  :all-builds [{:id "devcards"
                                :source-paths ["src/cljs" "src/cljc" "src/dev" "test/frontend"]
                                :figwheel { :devcards true } ;; <- note this
                                :compiler {:main       "dev.cards"
                                           :asset-path "js/compiled/devcards_out"
                                           :output-to  "resources/public/js/compiled/cards.js"
                                           :output-dir "resources/public/js/compiled/devcards_out"
                                           :source-map-timestamp true }}]})
(cljs-repl)
