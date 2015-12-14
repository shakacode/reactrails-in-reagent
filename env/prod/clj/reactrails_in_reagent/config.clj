(ns reactrails-in-reagent.config
  (:require [taoensso.timbre :as timbre]))

(def defaults
  {:init
   (fn []
     (timbre/info "\n-=[reactrails-in-reagent started successfully]=-"))
   :middleware identity})
