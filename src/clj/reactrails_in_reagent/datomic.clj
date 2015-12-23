(ns reactrails-in-reagent.datomic
  (:require
    [datomic.api :as d]
    [com.stuartsierra.component :as component]))



(defrecord DatomicDatabase [uri connection]
  component/Lifecycle
  (start [component]
    (if (:started? component)
      component
      (do (println "creating db")
          (d/create-database uri)
          (assoc component
            :connection (d/connect uri)
            :started? true))))
  (stop [component]
    (dissoc component :connection :started?)))

(defn make-database [db-uri]
  (DatomicDatabase. db-uri nil))


;; ---------------------------------------------------------------------------

(defrecord DatomicSchemaInstaller [schema database]
  component/Lifecycle
  (start [component]
    (if (:started? component)
      component
      (do (println "instaling schema")
          @(d/transact (-> component :database :connection) schema)
          (assoc component :started? true))))
  (stop [component]
    (dissoc component :started?)))

(defn make-schema-installer [schema]
  (DatomicSchemaInstaller. schema nil))



;; ---------------------------------------------------------------------------

(defn start-datomic-seeder [seeder]
  (println "start seeding")

  (try
    @(d/transact (-> seeder :database :connection)
                 (:seed-data seeder))
    (catch Exception e
      (println e)
      (throw e)))

  (println "seeded!")
  (assoc seeder
    :started? true))

(defrecord DatomicSeeder [seed-data database]
  component/Lifecycle
  (start [component]
    (if (:started? component)
      component
      (start-datomic-seeder component)))
  (stop [component]
    (dissoc component :started?)))

(defn make-seeder [seed-data]
  (DatomicSeeder. seed-data nil))

;; ---------------------------------------------------------------------------


(defrecord DatomicDeleter [uri]
  component/Lifecycle
  (start [component] component)
  (stop [component]
    (println "deleting database")
    (d/delete-database (:uri component))
    component))


(def make-db-deleter ->DatomicDeleter)