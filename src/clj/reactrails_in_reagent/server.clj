(ns reactrails-in-reagent.server
  (:require
    [com.stuartsierra.component :as component]
    [immutant.web :as web]))

(defn- start-web-server [component]
  (println "starting web server")
  (let [{:keys [handler-component options]} component
        handler (:handler handler-component)
        server (web/run handler options)]
    (assoc component
      :server server
      :started? true)))

(defn stop-web-server [component]
  (println "stopping web server")
  (web/stop)
  (dissoc component :handler :server :started?))

(defrecord WebServer [options]
  component/Lifecycle
  (start [component]
    (if (:started? component)
      component
      (try
        (start-web-server component)
        (catch Exception e
          (println e)
          (throw e)))))
  (stop [component]
    (try
      (stop-web-server component)
      (catch Exception e
        (println e)
        (throw e)))))


(defn make-web-server
  ([]
    (make-web-server {}))
  ([options]
    (WebServer. options)))





