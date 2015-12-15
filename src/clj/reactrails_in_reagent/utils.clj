(ns reactrails-in-reagent.utils
  (:require
    [datomic.api :as d]
    [clojure.pprint :as pp]))


(defn assoc-tempid
  ([m]
   (assoc-tempid m :db.part/user))
  ([m part]
   (assoc m :db/id (d/tempid part)))
  ([m part id]
   (assoc m :db/id (d/tempid part id))))