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


(defn add-comment [app comment]
  (-> (peridot/session app)
      (peridot/request (bidi/path-for routes 'comments/comment-list)
                       :request-method :post
                       :content-type "application/json"
                       :body (json/generate-string comment))))


(def comment1 {:author "1"
               :text   "1"})

(def comment2 {:author "2"
               :text   "2"})

(def wrong-comment {:a :a
                    :b :b
                    :c :c})


(defn test-adding-malformed [app]
  (let [res (add-comment app wrong-comment)]
    (given res
           [:response :status] := 400)))

(def comment-coercer
  (coerce/coercer schemas/Comment {schemas/date-schema schemas/date-matcher}))

(defn test-adding-wellformed [app comment]
  (let [res (add-comment app comment)
        redirected (peridot/follow-redirect res)
        body (get-in redirected [:response :body])
        decoded (json/parse-string body true)]
    (given res
           [:response :status] := 303)
    (given redirected
           [:response :status] := 200)
    (given decoded
           [] :? comment-coercer)))

(deftest adding-comments
  (let [app-dev (handler-dev)
        app-prod (handler-prod)]

    (testing "with wrong comment"
      (testing "with dev handler"  (test-adding-malformed app-dev))
      (testing "with prod handler" (test-adding-malformed app-prod)))

    (testing "with well formed comments"
      (testing "with dev handler" (test-adding-wellformed app-dev comment1))
      (testing "with prod handler" (test-adding-wellformed app-prod comment2)))))


