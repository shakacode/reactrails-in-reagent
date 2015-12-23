(ns reactrails-in-reagent.routes
  (:require
    [bidi.bidi :as bidi]
    [com.rpl.specter :as s]
    [com.rpl.specter.protocols]))

;; patch specter so that it behaves the same way for symbols than it does for keywords in paths.




(def comments-routes  ["/comments" {(bidi/alts "" "/") 'comments/comment-list
                                    ["/" :id] 'comments/comment-entry}])



(def routes ["" [[(bidi/alts "" "/") 'index]
                 comments-routes
                 [true 'miss-404]]])




#?(:clj
   (do
     (extend-type clojure.lang.Symbol
       com.rpl.specter.protocols/StructurePath
       (select* [kw structure next-fn]
         (next-fn (get structure kw)))
       (transform* [kw structure next-fn]
         (assoc structure kw (next-fn (get structure kw)))
         ))


     (def endpoints-path (s/walker symbol?))

     (defn endpoints [routes]
       "Returns a list of endpoints."
       (s/select endpoints-path routes))




     (defn inject-handlers [routes endpoints->handler]
       "Walks the routes datastructure and replaces endpoints names (symbols) with
       the actual handler."
       (s/transform endpoints-path
                    (fn [v] (get endpoints->handler v v))
                    routes))))

