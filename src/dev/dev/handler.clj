(ns dev.handler
  (:require
    [reactrails-in-reagent.handler :as handler]
    [reactrails-in-reagent.routes :as routes]
    [com.stuartsierra.component :as component]
    [ring.middleware.json :refer [wrap-json-params]]
    [clojure.pprint :as pp]
    [bidi.ring]))


;; Define a handler
(def handler-dev (-> routes/routes
                     (routes/inject-handlers handler/end-points->handlers)
                     (bidi.ring/make-handler)))

(defn start-dev-handler [component]
  (println "Starting DevHandler")
  (let [middleware ((:general-middleware component) component)]
    (assoc component
      :started? true
      :handler (middleware #'handler-dev))))

(defrecord DevHandler [routes-definition general-middleware]
  component/Lifecycle
  (start [component]
    (if (:started? component)
      component
      (start-dev-handler component)))
  (stop [component]
    (println "Stoping DevHandler")
    (dissoc component :handler :started?)))


(defn make-dev-handler [routes-def general-middleware]
  (DevHandler. routes-def general-middleware))


