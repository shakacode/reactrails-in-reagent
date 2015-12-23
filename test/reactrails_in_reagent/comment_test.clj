(ns reactrails-in-reagent.comment-test
  (:require
    [reactrails-in-reagent.comment :as comments]
    [reactrails-in-reagent.routes :refer [routes]]
    [bidi.bidi :as bidi]
    [schema.core :as s]
    [schema.coerce :as coerce]
    [reactrails-in-reagent.comment.schemas :as schemas]
    [reactrails-in-reagent.system-test :as sys]
    [clojure.test :as t :refer [deftest testing is are]]
    [juxt.iota :refer [given]]
    [peridot.core :as peridot]
    [cheshire.core :as json]))


(defonce system (atom nil))


(defn fresh-system! []
  (sys/install-system! system (sys/make-system))
  (sys/start! system))

(defn system-fixture [f]
  (fresh-system!)
  (f)
  (sys/stop! system))

(t/use-fixtures :each system-fixture)


(defn handler-prod [] (-> @system :handler-prod :handler))
(defn handler-dev [] (-> @system :handler-dev :handler))
(defn conn [] (-> @system :db :connection))


(defn clj-map->JSON
  "Translate to JSON as the browser does."
  [form]
  (let [form (reduce-kv (fn [res k v]
                          (assoc res (-> k name keyword) v))
                        {}
                        form)]
    (json/generate-string form)))


(def comment1 {:comment/author "1"
               :comment/text   "1"})

(def comment2 {:comment/author "2"
               :comment/text   "2"})

(def wrong-comment {:a :a
                    :b :b
                    :c :c})

(defn add-comment! [app comment]
  (-> (peridot/session app)
      (peridot/request (bidi/path-for routes 'comments/comment-list)
                       :request-method :post
                       :content-type "application/json"
                       :body (clj-map->JSON comment))))


(defn test-adding-malformed [app]
  (let [res (add-comment! app wrong-comment)]
    (given res
           [:response :status] := 400)))

(def comment-coercer
  (coerce/coercer schemas/Comment
                  {schemas/Comment #(json/parse-string % true)
                   schemas/date-schema schemas/date-matcher}))

(defn test-adding-wellformed [app comment]
  (let [res (add-comment! app comment)
        redirected (peridot/follow-redirect res)]
    (given res
           [:response :status] := 303)
    (given redirected
           [:response :status] := 200
           [:response :body] :? comment-coercer)
    (let [body (get-in redirected [:response :body])]
      (given (comment-coercer body)
             :comment/author := (:comment/author comment)
             :comment/text := (:comment/text comment)))))

(deftest adding-comments
  (let [app-dev (handler-dev)
        app-prod (handler-prod)]

    (testing "with wrong comment"
      (testing "with dev handler"  (test-adding-malformed app-dev))
      (testing "with prod handler" (test-adding-malformed app-prod)))

    (testing "with well formed comments"
      (testing "with dev handler" (test-adding-wellformed app-dev comment1))
      (testing "with prod handler" (test-adding-wellformed app-prod comment2)))))


(defn test-fetching-one-comment [app id comment]
  (let [c (-> (peridot/session app)
              (peridot/request (bidi/path-for routes 'comments/comment-entry :id id))
              :response
              :body)
        coerced (comment-coercer c)]
    (given coerced
           [] :- schemas/Comment
           :comment/author := (:comment/author comment)
           :comment/text := (:comment/text comment))))

(defn test-fetching-non-existant-comment [app]
  (let [res (-> (peridot/session app)
                (peridot/request (bidi/path-for routes 'comments/comment-entry :id 1)))]
    (given res
           [:response :status] := 404)))

(deftest fetching-one-comment
  (let [comment{:comment/author "1" :comment/text "t1"}
        c-id (comments/transact-new-comment (conn) comment)]

    (testing "when the comment does exist"
      (testing "with dev handler" (test-fetching-one-comment (handler-dev) c-id comment))
      (testing "with prod handler" (test-fetching-one-comment (handler-prod) c-id comment))))

  (testing "when the comment doesn't exist"
    (testing "with dev handler") (test-fetching-non-existant-comment (handler-dev))
    (testing "with dev handler") (test-fetching-non-existant-comment (handler-prod))))

(t/run-tests)
