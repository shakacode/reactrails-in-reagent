(ns reactrails-in-reagent.system
  (:require
    [reactrails-in-reagent.datomic :as datomic]
    [reactrails-in-reagent.server :as server]
    [reactrails-in-reagent.handler :as handler]
    [reactrails-in-reagent.routes :refer [routes]]
    [reactrails-in-reagent.utils :refer [read-edn-ressource]]

    [com.stuartsierra.component :as component]

    [ring.middleware.resource :refer [wrap-resource]]))



(defn middleware [_]
  (comp #(wrap-resource % "public")))

(defn config []
  {:db-uri "datomic:mem://example"
   :schema (read-edn-ressource "data/schema.edn")
   :server-config {:port 8080}
   :handler-config [routes
                    handler/end-points->handlers
                    handler/end-points->middlewares
                    middleware]})



(defn make-system-map [config]
  (component/system-map
    :db
    (datomic/make-database (:db-uri config))

    :schema-installer
    (datomic/make-schema-installer (:schema config))

    :web-request-handler
    (apply handler/make-handler (:handler-config config))

    :webserver
    (server/make-web-server (:server-config config))))

(defn dependency-map []
  {:schema-installer {:database :db}
   :web-request-handler {:database :db}
   :webserver {:handler-component :web-request-handler}})


(defn make-system [config]
  (-> (make-system-map config)
      (component/system-using (dependency-map))))



(defn -main [& _]
  (let [conf (config)
        sys (make-system conf)]
    (component/start sys)))
