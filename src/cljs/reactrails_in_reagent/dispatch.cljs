(ns reactrails-in-reagent.dispatch
  (:require
    [cljs.core.async :as async]
    [clojure.set :as set])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

;; greatly inspired from https://github.com/krisajenkins/petrol

(defprotocol Action
  (process-action* [action app]
                   "Given an action and an app state,
                   returns a new app state. In essense this is a reducing
                   function with inverted arguments"))

(defn apply-action
  "Convenience function to reduce actions more easily."
  [app message]
  (process-action* message app))

(defprotocol EventSource
  (watch-channels [source]
                  "Returns a set of channels that will feed the dispatch loop."))


(def ^:private !dispatch-state (atom {:channels #{}
                                      :dispatch-channel nil}))



(defn- refresh-state [state chan]
  (let [{:keys [channels dispatch-channel]} state
        channels (-> channels
                     (disj dispatch-channel)
                     (conj chan))]
    {:channels channels
     :dispatch-channel chan}))

(defn- remove-closed-channel! [c]
  (swap! !dispatch-state update :channels #(disj % c)))

(defn- apply-message-consequence! [!app-db m]
  (swap! !app-db apply-action m))

(defn- add-event-source! [source]
  (swap! !dispatch-state update :channels #(set/union % (watch-channels source))))


;; TODO refactor in a way that we can pass the dispatch loop's state as a parameter.
(defn start-dispatcher! [!app-db]
  (let [dispatch-c (async/chan)]

    ; set up dispatch state
    (swap! !dispatch-state refresh-state dispatch-c)

    ; start dispatch loop
    (go-loop
      []
      (when-let [channels (-> !dispatch-state deref :channels seq)]

        (let [[message channel] (async/alts! channels)]
          (when (nil? message)
            (remove-closed-channel! channel))

          (when (satisfies? Action message)
            (apply-message-consequence! !app-db message))

          (when (satisfies? EventSource message)
            (add-event-source! message)))
        (recur)))))


(defn dispatch! [message]
  (println "dispatching: " message)
  (async/put! (:dispatch-channel @!dispatch-state) message))



