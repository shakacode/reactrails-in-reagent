(ns user
  (:require
    [reloaded.repl :refer [system init start stop go ]]
    [reactrails-in-reagent.system :refer [make-system dev-config]]

    [datomic.api :as d]
    ))

(reloaded.repl/set-init! #(make-system dev-config))





(comment

  (def db (-> system :db ))

  (d/q '[:find ?e
         :where
         [?e :comment/text]]
       (-> db :connection d/db))

  (d/pull (-> db :connection d/db) '[*] 17592186045425)

  (go)


  (stop)

  )