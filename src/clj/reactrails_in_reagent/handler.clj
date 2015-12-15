(ns reactrails-in-reagent.handler
  (:require
    [reactrails-in-reagent.handler.utils :as h-utils]
    [reactrails-in-reagent.comment :as comments]
    [com.rpl.specter :as s]
    [bidi.bidi :as bidi]
    [bidi.ring]
    [ring.middleware.params :refer [wrap-params]]
    [com.stuartsierra.component :as component]
    ))

(def routes ["" [["/" :index]
                 ["/comments" {"" :comments/comment-list
                               ["/" :id] :comments/comment-entry}]
                 ["/test-conn" :test]]])


(h-utils/register-handler! :index h-utils/hello-response)
(h-utils/register-handler! :test h-utils/test-handler)
(h-utils/register-handler! :comments/comment-list comments/comment-list)
(h-utils/register-handler! :comments/comment-entry comments/comment-entry)





(defn make-transformations [handler-component]
  {(s/multi-path :comments/comment-list
                 :comments/comment-entry
                 :test)
   #(-> %
        (h-utils/wrap-dump-reg)
        (h-utils/wrap-assoc-request :conn (-> handler-component :database :connection)))})

(defn apply-transformations [transforms data]
  (reduce-kv (fn [res path transformation]
               (s/transform path transformation res))
             data transforms))

(defn inject-handlers [routes-def handlers]
  (s/transform (s/walker keyword?)
               (fn [value] (get handlers value value))
               routes-def))

(defn prepare-routes [routes handlers transformations]
  (inject-handlers routes
                   (apply-transformations transformations handlers )))

(defn compute-handler [routes handlers transformations]
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


(comment

  (def handlers' {:index :index-stuff
                 :comments/comment-list :comments/comment-list-stuff
                 :comments/comment-entry :comments/comment-entry-stuff
                 :test :test-stuff})

  (bidi/path-for routes :specific-comment :id 1)

  (def ex-transforms {(s/multi-path :comments/comment-list
                                    :comments/comment-entry)
                      (fn [kw] (name kw))

                      :index str})

  (apply-transformations ex-transforms handlers')

  routes
  (prepare-routes routes handlers' ex-transforms)


  (s/transform (s/multi-path :comments/comment-list
                             :comments/comment-entry)
               vector
               dispach')
  (bidi/match-route routes "/"
                    :request-method :get)

  (bidi/match-route routes "/comment"
                    :request-method :get)

  )