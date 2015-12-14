(ns reactrails-in-reagent.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [reactrails-in-reagent.db.core :as db]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]))

(s/defschema Comment {(s/optional-key :id) s/Num
                      :name s/Str
                      :content s/Str
                      (s/optional-key :created_at) java.util.Date})

(defn extract-id
  "extract generated id from h2 result"
  [res]
  ((keyword "scope_identity()") res))

(defapi service-routes
  (ring.swagger.ui/swagger-ui
   "/swagger-ui")
  ;JSON docs available at the /swagger.json route
  (swagger-docs
    {:info {:title "Comment api"}})
  (context* "/api" []
            :tags ["comments"]

            (GET* "/comments" []
                  :return       [Comment]
                  :query-params []
                  :summary      "todays comments"
                  (do
                    (Thread/sleep 200)
                    (ok (db/get-comments-after {:after (-> (t/now)
                                                           (t/plus (t/days (- 1)))
                                                           tc/to-date)}))))
            (PUT* "/comments" []
                  :return       Comment
                  :body         [comment Comment]
                  :summary      "add new comment"
                  (let [comment (assoc comment :created_at (new java.util.Date))]
                    (Thread/sleep 200)
                    ; (not-implemented "error")
                    (ok (assoc comment :id (extract-id (db/create-comment<! comment))))))))
