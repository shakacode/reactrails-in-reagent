(ns reactrails-in-reagent.utils
  (:require
    [datomic.api :as d]
    [clojure.java.io :as io])
  (:import
    (datomic Util)))


(defn assoc-tempid
  ([m]
   (assoc-tempid m :db.part/user))
  ([m part]
   (assoc m :db/id (d/tempid part)))
  ([m part id]
   (assoc m :db/id (d/tempid part id))))


(defn read-edn-ressource [file-name]
  (-> file-name io/resource io/reader Util/readAll first))