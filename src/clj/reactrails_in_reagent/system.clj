(ns reactrails-in-reagent.system
  (:require
    [reactrails-in-reagent.datomic :as datomic]
    [reactrails-in-reagent.server :as server]
    [reactrails-in-reagent.handler :as handler]
    [reactrails-in-reagent.handler.utils :as h-utils]

    [com.stuartsierra.component :as component]
    [clojure.java.io :as io]

    [ring.middleware.reload :refer [wrap-reload]]
    [ring.middleware.stacktrace :refer [wrap-stacktrace]]
    [ring.middleware.params :refer [wrap-params]]

    [liberator.dev :refer [wrap-trace]])
  (:import (datomic Util)))


(defn read-edn-ressource [file-name]
  (-> file-name io/resource io/reader Util/readAll first))


(def middleware-dev (comp wrap-stacktrace
                          wrap-reload
                          #(wrap-trace % :header :ui)
                          wrap-params))


(def dev-config {:db-uri "datomic:mem://example"
                 :schema (read-edn-ressource "data/schema.edn")
                 :seed-data (read-edn-ressource "data/seed.edn")
                 :server-config {:port 8080}
                 :middleware middleware-dev
                 :handler-config [handler/routes
                                  (h-utils/get-handlers-dev)
                                  handler/make-transformations]})

dev-config

(defn make-system-map [config]
  (component/system-map
    :db
    (datomic/make-database (:db-uri config))

    :schema-installer
    (datomic/make-schema-installer (:schema config))

    :seeder
    (datomic/make-seeder (:seed-data config))

    :web-request-handler
    (apply handler/make-handler (:handler-config config))

    :webserver
    (server/make-web-server (:server-config config) (:middleware config))))

(defn dependency-map []
  {:schema-installer {:conn :db}
   :seeder {:database :db
            :schema-installer :schema-installer}
   :web-request-handler {:database :db}
   :webserver {:database :db
               :handler-component :web-request-handler}})

(defn make-system [config]
  (-> (make-system-map config)
      (component/system-using (dependency-map))))