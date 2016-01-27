(ns reactrails-in-reagent.dispatch-test
  (:require
    [devcards.core :as devcards]
    [reactrails-in-reagent.dispatch :as dispatch]
    [reactrails-in-reagent.core :as core]
    [reactrails-in-reagent.actions :as actions]
    [cljs.core.async :as async])
  (:require-macros [devcards.core :as dc :refer [defcard defcard-doc deftest]]
                   [cljs.test :as t :refer [testing is]]
                   [cljs.core.async.macros :as async]))


(defcard-doc
  "
  # The dispatcher in RinR.

  ## A dispatcher intro

  The `reactrails-in-reagent` front end is driven by an event loop similar to
  the one found in [Petrol](https://github.com/krisajenkins/petrol).
  Here is the code for the event loop: "

  (dc/mkdn-pprint-source dispatch/start-dispatcher!)

  "The function has two arities [!app-db] & [!app-db !dispatch-state].
  The job is done in the second arity, the first giving  default value
  for the dispatch state:"

  (dc/mkdn-pprint-source dispatch/!dispatch-state)

  "The `start-dispatcher` fn has in fact two purposes:

  It initializes some dispatch state the loop will need, possibly from a previous one.
  This is done by swapping on the !dispatch-state atom with:
  ```clojure
  (swap! !dispatch-state fresh-state dispatch-c)
  ```
  and:"

  (dc/mkdn-pprint-source dispatch/fresh-state)

  "
  It starts a core.async go loop that handles events. This loop does 4 things:

  - First: recovers the channels to listen to. If there aren't any stop looping, if there are select one
  and perfom an iteration
    ```clojure
    (when-let [channels (-> !dispatch-state deref :channels seq)]
      (let [[message channel] (async/alts! channels)]
        ...)
      ...
      (recur))
    ```
  - Second: if the selected channel is closed remove it from the pool of pending ones
  ```clojure
  (when (nil? message)
    (remove-closed-channel! !dispatch-state channel))\n
  ```
  - Third: if the message is implements the `Action` protocol apply the action to the app state
  ```clojure
  (when (satisfies? Action message)
    (apply-message-consequence! !app-db message))
  ```
  - Fourth: If the event implements the `EventSource` protocol recovers the channels from this event source
  and place them with the rest of the pending channels
  ```clojure
  (when (satisfies? EventSource message)
    (add-event-source! !dispatch-state message))
  ```

  ## The tests")

(deftest dispatching-actions
  "Dispatching an action doesn't make it execute right away, the first
  test should show no change, the second executed asynchronously should
  see the change."
   (let [app-state (atom (assoc core/initial-state :test true))
         dispatch-state (atom (assoc dispatch/initial-dispatch-state :test true))
         changed (async/chan)]
     (dispatch/start-dispatcher! app-state dispatch-state)
     (add-watch app-state :test
                (fn [& args]
                  (async/put! changed true)))
     (dispatch/dispatch! dispatch-state
                         (actions/->SelectFormStyle :stacked))
     (is (= (:nav/index core/initial-state)
            (:nav/index @app-state)
            :horizontal))

     (t/async done
       (async/go
         (async/<! changed)
         (is (= :stacked
                (:nav/index @app-state)))
         (done)))))


(deftest dispatching-event-source
  "Here we check that:

  1. after dispatching an `EventSource, the dispatcher state has merged
   the new channel `pending-channel` provided by it
  2. once we've `put!` an action in our `pending-channel`
  this action is properly executed
  3. `pending-channel` is removed from the dispatch state when closed"
  (let [app-state (atom (assoc core/initial-state ::test true))
        dispatch-state (atom (assoc dispatch/initial-dispatch-state ::test true))
        app-state-changed (async/chan)
        dispatch-state-changed (async/chan)
        pending-channel (async/chan)]
    (dispatch/start-dispatcher! app-state dispatch-state)
    (add-watch app-state ::test
               (fn [& args]
                 (async/put! app-state-changed true)))
    (add-watch dispatch-state ::test
               (fn [& args]
                 (async/put! dispatch-state-changed true)))
    (dispatch/dispatch! dispatch-state
                        (reify dispatch/EventSource
                          (watch-channels [_] #{pending-channel})))

    (t/async done
      (async/go
        (async/<! dispatch-state-changed)
        (is (contains? (:channels @dispatch-state) pending-channel))
        (async/>! pending-channel (actions/->SelectFormStyle :inline))

        (async/<! app-state-changed)
        (is (= :inline (:nav/index @app-state)))

        (async/close! pending-channel)
        (async/<! dispatch-state-changed)
        (is (not (contains? (:channels @dispatch-state) pending-channel)))
        (done)))))