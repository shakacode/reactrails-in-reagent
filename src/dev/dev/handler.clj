(ns dev.handler
  (:require
    [reactrails-in-reagent.handler :as handler]
    [reactrails-in-reagent.routes :as routes]
    [com.stuartsierra.component :as component]
    [ring.middleware.json :refer [wrap-json-params]]
    [clojure.pprint :as pp]
    [bidi.ring]))


(defn test-handler [request]
  (pp/pprint request)
  {:status 200
   :body "test123"})

(defn add-test-handler [routes]
  (let [[root actual-routes] routes
        catch-all (last actual-routes)
        new-routes (-> actual-routes
                       (pop)
                       (conj ["/test" test-handler] catch-all))]
    [root new-routes]))



;; Define a handler
(def handler-dev (-> routes/routes
                     (add-test-handler)
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

(let [[_ rs] routes/routes
      ]
  (last rs))

