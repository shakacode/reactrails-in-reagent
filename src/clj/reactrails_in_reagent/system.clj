(ns reactrails-in-reagent.system
  (:require
    [reactrails-in-reagent.datomic :as datomic]
    [reactrails-in-reagent.server :as server]
    [reactrails-in-reagent.handler :as handler]
    [reactrails-in-reagent.routes :refer [routes]]
    [reactrails-in-reagent.utils :refer [read-edn-ressource]]

    [com.stuartsierra.component :as component]
    [environ.core :refer [env]]

    [ring.middleware.resource :refer [wrap-resource]]))

(defn middleware [_]
  (comp #(wrap-resource % "public")))

(defn config []
  {:db-uri         "datomic:mem://example"
   :schema         (read-edn-ressource "data/schema.edn")
   :server-config  {:port (Integer/parseInt (env :port "8080"))
                    :host (env :immutant-host "127.0.0.1")}
   :handler-config [routes
                    handler/end-points->handlers
                    handler/end-points->middlewares
                    middleware]})


(def system-map
  {:db
   #(datomic/make-database (:db-uri %))

   :schema-installer
   #(datomic/make-schema-installer (:schema %))

   :web-request-handler
   #(apply handler/make-handler (:handler-config %))

   :webserver
   #(server/make-web-server (:server-config %))})

(defn apply-config [sys-map config]
  (reduce-kv (fn [acc k v]
               (assoc acc k (v config)))
             {}
             sys-map))

(defn make-system-map [config]
  (-> system-map
      (apply-config config)
      (component/map->SystemMap )))

(defn dependency-map []
  {:schema-installer {:database :db}
   :web-request-handler {:database :db}
   :webserver {:handler-component :web-request-handler}})


(defn make-system [config]
  (component/system-using (make-system-map config)
                          (dependency-map)))



