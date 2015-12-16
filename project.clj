(defproject reactrails-in-reagent "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-Xms512m" "-Xmx512m" "-server"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.datomic/datomic-free "0.9.5344"]
                 [bidi "1.23.1"]
                 [org.immutant/web "2.1.1"]
                 [liberator "0.13"]
                 [ring/ring-core "1.4.0"]
                 [com.rpl/specter "0.9.0"]
                 [prismatic/schema "1.0.4"]

                 ;[com.cognitect/transit-clj "0.8.281"]
                 ;[com.cognitect/transit-cljs "0.8.225"]
                 [com.stuartsierra/component "0.2.3"]]

  :profiles {:dev {:dependencies [[reloaded.repl "0.2.1"]
                                  [ring/ring-devel "1.4.0"]
                                  [ring/ring-mock "0.3.0"]

                                  [compojure "1.0.2"]]}}

  :source-paths ["src/clj"
                 "src/cljs"
                 "src/dev"]
  )