(ns reactrails-in-reagent.handler
  (:require
    [reactrails-in-reagent.handler.utils :as h-utils]
    [reactrails-in-reagent.comment :as comments]
    [com.rpl.specter :as s]
    [bidi.bidi :as bidi]
    [bidi.ring]
    [ring.middleware.params :refer [wrap-params]]
    [ring.util.response :refer [resource-response]]
    [com.stuartsierra.component :as component]
    ))

(def routes ["" [[(bidi/alts "" "/") :index]
                 comments/routes]])


(defn index [_]
  (assoc (resource-response (str "html/index.html") {:root "public"})
    :headers {"Content-Type" "text/html"}))

(h-utils/register-handler! :index index)


(defn make-transformations [handler-component]
  (merge {}
         (comments/make-transformations handler-component)))

(defn apply-transformations
  "Apply the transformations to each of the routes."
  [transforms spector-selector->route]
  (reduce-kv (fn [res path transformation]
               (s/transform path transformation res))
             spector-selector->route transforms))

(defn inject-handlers
  "Walks the routes datastructure and replaces ids for handlers with
  the actual handler."
  [routes-def handlers]
  (s/transform (s/walker keyword?)
               (fn [value] (get handlers value value))
               routes-def))

(defn prepare-routes
  "Transforms the actuals handlers and injects them into the
  datastructure representing the routes."
  [routes handlers transformations]
  (inject-handlers routes
                   (apply-transformations transformations handlers )))

(defn compute-handler
  "Prepares the routes datastructure then turns it into a proper ring handler."
  [routes handlers transformations]
  (bidi.ring/make-handler
    (prepare-routes routes handlers transformations)))


(defn start-handler [component]
  (println "assembling handler")
  (assoc component
    :handler (compute-handler (:routes-definition component)
                              (:handlers-fn component)
                              ((:transforms-fn component) component))
    :started? true))

(defrecord Handler [routes-definition handlers-fn transforms-fn]
  component/Lifecycle
  (start [component]
    (if (:started? component)
      component
      (start-handler component)))
  (stop [component]
    (dissoc component :handler :started?)))

(defn make-handler [routes-definition handlers-fn transforms-fn]
  (Handler. routes-definition handlers-fn transforms-fn))

