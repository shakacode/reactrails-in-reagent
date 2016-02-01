(ns reactrails-in-reagent.routes
  (:require
    [bidi.bidi :as bidi]
    [com.rpl.specter :as s]
    [com.rpl.specter.protocols]))

(def index-route [(bidi/alts "" "/") :index])

(def missed-route [true :miss-404])

(def comments-routes ["/comments" {(bidi/alts "" "/") :comments/comment-list
                                   ["/" :id]          :comments/comment-entry}])

(def routes ["" [index-route
                 comments-routes
                 missed-route]])

(defn path-for [endpoint-name & params]
  (apply bidi/path-for routes endpoint-name params))

(def endpoints-path (s/walker keyword?))

(defn inject-handlers [routes endpoints->handler]
  "Walks the `routes` datastructure and replaces endpoints names with
  the actual handlers for the endpoints as defined in the mapping
  `endpoints->handler`."
  (s/transform endpoints-path
               (fn [v] (get endpoints->handler v v))
               routes))
