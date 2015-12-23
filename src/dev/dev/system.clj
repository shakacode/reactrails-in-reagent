(ns dev.system
  (:require
    [reactrails-in-reagent.datomic :as datomic]
    [reactrails-in-reagent.server :as server]
    [dev.handler :as handler]
    [reactrails-in-reagent.routes :refer [routes]]
    [reactrails-in-reagent.utils :refer [read-edn-ressource]]


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
        #(wrap-assoc-request % :conn (-> handler-component :database :connection)
                             :routes (-> handler-component :routes-definition))))





(def config {:db-uri "datomic:mem://example"
             :schema (read-edn-ressource "data/schema.edn")
             :seed-data (read-edn-ressource "data/seed.edn")
             :server-config {:port 8080}
             :handler-config [routes middleware]})


(defn make-system-map [config]
  (component/system-map
    :db
    (datomic/make-database (:db-uri config))

    :schema-installer
    (datomic/make-schema-installer (:schema config))

    :seeder
    (datomic/make-seeder (:seed-data config))

    :web-request-handler
    (apply handler/make-dev-handler (:handler-config config))

    :webserver
    (server/make-web-server (:server-config config))))

(def dependency-map
  {:schema-installer {:database :db}
   :seeder {:database :db
            :schema-installer :schema-installer}
   :web-request-handler {:database :db}
   :webserver {:handler-component :web-request-handler}})

(defn make-system [config]
  (-> (make-system-map config)
      (component/system-using dependency-map)))



