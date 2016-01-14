(ns user
  (:require
    [reloaded.repl :refer [system init start stop go ]]
    [dev.system :refer [make-system config]]
    [reactrails-in-reagent.system :as prod]

    [datomic.api :as d]

    [cheshire.core :refer [generate-string]]
    ))


(defn install-dev []
  (reloaded.repl/set-init! #(make-system (config))))


(defn install-prod []
  (reloaded.repl/set-init! #(prod/make-system (prod/config))))


(install-dev)

(comment
  (install-dev)

  (install-prod)

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


  (count all)

  (generate-string all)

  (d/pull (-> db :connection d/db) '[*] 17592186045418)

  )