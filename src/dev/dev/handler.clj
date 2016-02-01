(ns dev.handler
  (:require
    [reactrails-in-reagent.handler :as handler]
    [reactrails-in-reagent.routes :as routes]
    [com.stuartsierra.component :as component]
    [ring.util.response :refer [resource-response]]
    [com.rpl.specter :as specter]
    [bidi.ring]))

(defn devcards-handler [_]
  (-> "cards/cards-index.html"
      resource-response
      (assoc :headers {"Content-Type" "text/html"})))

(def devcards-route ["/devcards" devcards-handler])

(def routes (specter/setval [specter/LAST specter/BEGINNING]
                            [devcards-route]
                            routes/routes))

(def handler-dev (-> routes
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
