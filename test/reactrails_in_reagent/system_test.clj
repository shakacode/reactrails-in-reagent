(ns reactrails-in-reagent.system-test
  (:require
    [reactrails-in-reagent.datomic :as datomic]
    [reactrails-in-reagent.routes :as routes]
    [reactrails-in-reagent.handler :as handler-prod]
    [dev.handler :as handler-dev]
    [reactrails-in-reagent.utils :refer [read-edn-ressource]]

    [com.stuartsierra.component :as component]

    [reactrails-in-reagent.system :as prod]
    [dev.system :as dev]))




(def config {:db-uri        "datomic:mem://example"
             :schema        (read-edn-ressource "data/schema.edn")
             :seed-data     (read-edn-ressource "data/seed.edn")
             :server-config {:port 8080}
             :handler-dev  [routes/routes
                             dev/middleware]
             :handler-prod  [routes/routes
                             handler-prod/end-points->handlers
                             handler-prod/end-points->middlewares
                             prod/middleware]})



(defn make-system-map [config]
  (component/system-map
    :db
    (datomic/make-database (:db-uri config))

    :db-deleter
    (datomic/make-db-deleter (:db-uri config))

    :schema-installer
    (datomic/make-schema-installer (:schema config))

    :handler-prod
    (apply handler-prod/make-handler (:handler-prod config))

    :handler-dev
    (apply handler-dev/make-dev-handler (:handler-dev config))))


(def dependency-map
  {:schema-installer {:database :db}
   :handler-prod {:database :db}
   :handler-dev {:database :db}})


(defn make-system [& [opts]]
  (-> (make-system-map (merge config (or opts {})))
      (component/system-using dependency-map)))


;; utilities for make system fixtures in tests

(defn install-system! [ref sys]
  (swap! ref (fn [v]
               (when v (component/stop-system v))
               sys)))

(defn start! [ref]
  (swap! ref component/start-system))


(defn stop! [ref]
  (swap! ref component/stop-system))










