(ns reactrails-in-reagent.actions-test
  (:require
    [reactrails-in-reagent.core :as rinr-core]
    [reactrails-in-reagent.actions :as actions]
    [reactrails-in-reagent.dispatch :refer [process-message]])
  (:require-macros [devcards.core :refer [defcard deftest]]
                   [cljs.test :refer [testing is]]))

(deftest switching-form-view
  (testing "We can change the way the form is displayed"
    (is (= 1 (-> rinr-core/initial-state
                 (process-message (actions/->SelectFormStyle 1))
                 :nav/index)))
    (is (= 2 (-> rinr-core/initial-state
                 (process-message (actions/->SelectFormStyle 1))
                 (process-message (actions/->SelectFormStyle 2))
                 :nav/index)))))