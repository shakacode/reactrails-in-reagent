(ns reactrails-in-reagent.core
  (:require
    [reactrails-in-reagent.system :as system]
    [com.stuartsierra.component :as component]))



(defn -main [& _]
  (let [conf (system/config)
        sys (system/make-system conf)]
    (component/start sys)))