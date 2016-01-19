(ns test.runner
  (:require [jx.reporter.karma :refer-macros [run-tests run-all-tests]]
            [reactrails-in-reagent.actions-test]))


(defn ^:export run [karma]
  (run-tests karma 'reactrails-in-reagent.actions-test))
