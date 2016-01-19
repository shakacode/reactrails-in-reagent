(ns reactrails-in-reagent.actions-test
  (:require
    [devcards.core :as devcards]
    [reactrails-in-reagent.core :as rinr-core]
    [reactrails-in-reagent.actions :as actions]
    [reactrails-in-reagent.dispatch :as dispatch :refer [apply-action]])
  (:require-macros [devcards.core :as dc :refer [defcard defcard-doc deftest]]
                   [cljs.test :refer [testing is]]))

(defcard-doc
  "# Actions tests

  ## Overview
  We test here the different actions that can happen in the app.
  Each action in the app is a record in `reactrails-in-reagent.actions` and implements
  `reactrails-in-reagent.dispatch/Action`.

  At the start the app has the following state:"
  rinr-core/initial-state

  "We can for instance apply the action of changing the way the form is displayed"
  (dc/mkdn-pprint-code
    '(dispatch/apply-action rinr-core/initial-state (actions/->SelectFormStyle 1)))

  "and get the new value for the app state:"
  (dispatch/apply-action rinr-core/initial-state (actions/->SelectFormStyle 1))

  "## Tests"
  )

(deftest switching-form-view
  "We can change the way the form is displayed by changing the `:nav/index` key
  of the global state."

  (testing "At initialisation the nan/index is 0"
    (is (= 0 (:nav/index rinr-core/initial-state))))

  (testing "We can apply actions that change the :nav/index"
    (is (= 1 (:nav/index (apply-action rinr-core/initial-state (actions/->SelectFormStyle 1)))))
    (is (= 2 (:nav/index (reduce apply-action rinr-core/initial-state
                                 [(actions/->SelectFormStyle 1)
                                  (actions/->SelectFormStyle 2)]))))))


(def comments
  {:first {:comment/author  "Alice"
           :comment/text    "A text from alice"
           :comment/created (new js/Date 2016 01 01 12 0)
           :db/id           1}
   :second {:comment/author  "Bob"
            :comment/text    "A text from bob"
            :comment/created (new js/Date 2016 01 01 12 01)
            :db/id           2}
   :third {:comment/author  "Charlie"
           :comment/text    "Charlie comes into play"
           :comment/created (new js/Date 2016 01 01 12 03)
           :db/id           3}})

(def another-comment
  {:comment/author  "Alice"
   :comment/text    "Charlie comes into play"
   :comment/created (new js/Date 2016 01 01 12 04)
   :db/id           4})

(def state-when-comments-arrive-chronologically
  (-> comments
      (as-> cs
            ((juxt :first :second :third) cs)
            (map actions/->ReceivedComment cs)
            (reduce apply-action rinr-core/initial-state cs))))


(def state-when-comments-arrive-with-wrong-order
  (-> comments
      (as-> cs
            ((juxt :third :first :second) cs)
            (map actions/->ReceivedComment cs)
            (reduce apply-action rinr-core/initial-state cs))))

(def expected-state-in-any-case
  (update rinr-core/initial-state
          :comments into ((juxt :first :second :third) comments)))

(defcard-doc
  "Let's define somme values to test the reception of comments"
  (dc/mkdn-pprint-source comments)
  (dc/mkdn-pprint-source another-comment)
  (dc/mkdn-pprint-source state-when-comments-arrive-chronologically)
  (dc/mkdn-pprint-source state-when-comments-arrive-with-wrong-order)
  (dc/mkdn-pprint-source expected-state-in-any-case))


(deftest testing-new-comment-reception
  "Here we specify what should happen when the app receives a new comment"
  (testing "We received the comments in the wright order"
    (is (= (:comments expected-state-in-any-case )
           (:comments state-when-comments-arrive-chronologically))))

  (testing "We received comments in the wrong order"
    (is (= (:comments expected-state-in-any-case)
           (:comments state-when-comments-arrive-with-wrong-order)))))

(deftest receiving-all-comments
  "The app polls comments and receives them in bulk, dipatching the action
  `->ReceivedAllComments`"

  (is (= (:comments expected-state-in-any-case)
         (-> rinr-core/initial-state
             (apply-action (actions/->ReceivedAllComments (vals comments)))
             :comments))))


(deftest race-conditions
  "We need to make sure the ordering of the processing of events doesn't lead us to an unexpected state.
  If the client issues a request to get all comments then posts a new one, the app will process one
  `->ReceivedAllComment` action and one `->ReceivedNewComment` action. The ordering of the processing should not
  have any influence on the result."

  (is (= (-> rinr-core/initial-state
             (apply-action (actions/->ReceivedAllComments (vals comments)))
             (apply-action (actions/->ReceivedComment another-comment))
             :comments)

         (-> rinr-core/initial-state
             (apply-action (actions/->ReceivedComment another-comment))
             (apply-action (actions/->ReceivedAllComments (vals comments)))
             :comments))))
