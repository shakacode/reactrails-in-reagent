(ns reactrails-in-reagent.handler
  (:require
    [reactrails-in-reagent.common-handlers :as common]
    [reactrails-in-reagent.comment :as comments]
    [reactrails-in-reagent.routes :as routes]
    [bidi.ring]
    [com.stuartsierra.component :as component]

    [liberator.representation]
    [cheshire.core :refer [generate-string]]))


;; Patching liberator to use cheshire
(defmethod liberator.representation/render-map-generic "application/json" [data _]
  (generate-string data))

(defmethod liberator.representation/render-seq-generic "application/json" [data _]
  (generate-string data))


;; ----------------------------------------------------------------------
;; Regroup all of the app's handlers and their associated middleware
(def end-points->handlers
  (merge
    common/end-points->handlers
    comments/end-points->handlers))

(defn end-points->middlewares [handler-component]
  (merge
    (common/end-points->middlewares handler-component)
    (comments/end-points->middlewares handler-component)))


;; ----------------------------------------------------------------------
;; General handler building mechanics
(defn- apply-middleware [end-points->handlers end-point-name middelware]
  (update end-points->handlers end-point-name middelware))

(defn- apply-middlewares [end-points->handlers end-points->middlewares]
  (reduce-kv apply-middleware
             end-points->handlers
             end-points->middlewares))

(defn prepare-routes
  "Starts by applying middlewares to their respective handlers then injects
  the \"middlewared\" handlers inside the routes data-structure."
  [routes handlers middlewares]
  (routes/inject-handlers routes
                   (apply-middlewares handlers middlewares )))

(defn compute-handler
  "Prepares the routes datastructure then turns it into a proper ring handler."
  [routes handlers middleware-associations]
  (bidi.ring/make-handler
    (prepare-routes routes handlers middleware-associations)))


;; ----------------------------------------------------------------------
;; Definition of the handler component
(defn start-handler [handler-component]
  (println "assembling handler")
  (let [{:keys [routes-definition
                handlers
                middelware-associations
                general-middleware]} handler-component

        handler (compute-handler routes-definition
                                 handlers
                                 (middelware-associations handler-component))
        handler' ((general-middleware handler-component) handler)]
    (assoc handler-component
      :handler handler'
      :started? true)))

(defrecord Handler
  [routes-definition        ; bidi routes data-structure
   handlers                 ; map of endpoint-names -> handlers
   middelware-associations  ; function that given the component returns a map of endpoint-names -> middleware
   general-middleware]      ; top middleware used on every http call
  component/Lifecycle
  (start [component]
    (if (:started? component)
      component
      (start-handler component)))
  (stop [component]
    (dissoc component :handler :started?)))

(defn make-handler [routes-definition handlers middleware-association general-middleware]
  (Handler. routes-definition handlers middleware-association general-middleware))
