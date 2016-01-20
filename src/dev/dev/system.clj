(ns dev.system
  (:require
    [reactrails-in-reagent.system :as system]
    [reactrails-in-reagent.datomic :as datomic]
    [reactrails-in-reagent.routes :refer [routes]]
    [reactrails-in-reagent.utils :refer [read-edn-ressource]]
    [dev.handler :as handler]


    [com.stuartsierra.component :as component]

    [ring.middleware.params :refer [wrap-params]]
    [ring.middleware.json :refer [wrap-json-params]]
    [ring.middleware.reload :refer [wrap-reload]]
    [ring.middleware.stacktrace :refer [wrap-stacktrace]]
    [ring.middleware.resource :refer [wrap-resource]]
    [reactrails-in-reagent.handler.middleware :refer [wrap-assoc-request]]

    [liberator.representation]
    [cheshire.core :refer [generate-string]]

    [liberator.dev :refer [wrap-trace]]))






(defn middleware [handler-component]
  (comp #(wrap-resource % "public")
        wrap-stacktrace
        wrap-reload
        #(wrap-trace % :header :ui)
        wrap-json-params
        wrap-params
        #(wrap-assoc-request % :conn (-> handler-component :database :connection))))

(defn config []
  (-> (system/config)
      (assoc
        :seed-data (read-edn-ressource "data/seed.edn")
        :handler-config [routes middleware])))

;; TODO See if there is a way to use suspendable to recompute the handle

(defn make-system-map [config]
  (-> system/system-map
      (assoc :seeder
             #(datomic/make-seeder (:seed-data %))

             :web-request-handler
             #(apply handler/make-dev-handler (:handler-config %)))
      (system/apply-config config)
      (component/map->SystemMap)))

(defn dependency-map []
  (assoc (system/dependency-map)
    :seeder {:database :db
             :schema-installer :schema-installer}))

(defn make-system [config]
  (-> (make-system-map config)
      (component/system-using (dependency-map))))

