(ns reactrails-in-reagent.config
  (:require [selmer.parser :as parser]
            [taoensso.timbre :as timbre]
            [reactrails-in-reagent.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (timbre/info "\n-=[reactrails-in-reagent started successfully using the development profile]=-"))
   :middleware wrap-dev})
