(ns user
  (:require
    [reloaded.repl :refer [system init start stop go]]
    [dev.system :as dev]
    [reactrails-in-reagent.system :as prod]

    [datomic.api :as d]

    [cheshire.core :refer [generate-string]]

    [figwheel-sidecar.repl-api :as fig-api]

    [clojure.pprint :as pp]))


(defn install-dev []
  (reloaded.repl/set-init! #(dev/make-system (dev/config))))

(defn install-prod []
  (reloaded.repl/set-init! #(prod/make-system (prod/config))))


(defn start-figwheel! [build-id]
  (fig-api/start-figwheel!)
  (fig-api/switch-to-build build-id)
  (fig-api/cljs-repl))



(install-dev)



(comment
  (install-dev)
  (install-prod)
  (go)
  (stop)
  (start-figwheel! "dev")
  (start-figwheel! "devcards")

  )