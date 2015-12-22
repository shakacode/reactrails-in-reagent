(ns user
  (:require
    [reloaded.repl :refer [system init start stop go ]]
    [reactrails-in-reagent.system :refer [make-system dev-config]]

    [datomic.api :as d]

    [cheshire.core :refer [generate-string]]
    ))

(reloaded.repl/set-init! #(make-system dev-config))



(comment



  (go)
  (stop)


  (def db (-> system :db ))

  (d/q '[:find [(pull ?e [*]) ...]
         :where
         [?e :comment/text]]
       (-> db :connection d/db))


  (def all (d/q '[:find [(pull ?e [*]) ...]
                  :where
                  [?e :comment/text]]
                (-> db :connection d/db)))


  all

  (generate-string all)

  (d/pull (-> db :connection d/db) '[*] 17592186045418)

  )