(ns reactrails-in-reagent.common-handlers
  (:require
    [ring.util.response :refer [resource-response]]))



(defn index [_]
  (assoc (resource-response (str "html/index.html") {:root "public"})
    :headers {"Content-Type" "text/html"}))

(defn miss-404 [request]
  (assoc (resource-response (str "html/404.html") {:root "public"})
    :headers {"Content-Type" "text/html"}))


(def end-points->handlers
  {'index index
   'miss-404 miss-404})

(defn end-points->middlewares [_]
  {})