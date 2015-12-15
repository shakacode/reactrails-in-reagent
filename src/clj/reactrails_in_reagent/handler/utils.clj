(ns reactrails-in-reagent.handler.utils
  (:require
    [clojure.pprint :as pp]))


(def ^:private handlers (atom {}))

(defn register-handler!* [key h]
  {:pre [(var? h)]}
  (swap! handlers assoc key h))

(defmacro register-handler! [key h]
  {:pre [(symbol? h)]}
  (let [v (resolve h)]
    `(register-handler!* ~key ~v)))

(defn get-handlers-dev [] @handlers)

(defn deref-handlers [hs]
  (reduce-kv (fn [acc k v] (assoc acc k (deref v))) {} hs))

(defn get-handlers-prod []
  (deref-handlers @handlers))



(defn hello-response [request]
  {:status 200
   :body   "Hello world! yo dude refresh"})

(defn test-handler [request]
  (if-let [db-conn (:conn request)]
    (do (println db-conn)
        {:status 200
         :body   "yeah it works great please friend"})
    "shoot not quite there yet"))


(defn wrap-assoc-request [handler key value]
  (fn [request]
    (handler (assoc request key value))))

(defn wrap-dump-reg [handler]
  (fn [request]
    (pp/pprint request)
    (handler request)))