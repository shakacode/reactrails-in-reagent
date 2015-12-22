(ns reactrails-in-reagent.dispatch
  (:require
    [cljs.core.async :as async]
    [clojure.set :as set]
    [petrol.core :as p])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

;; greatly inspired from https://github.com/krisajenkins/petrol

(defprotocol Message
  (process-message* [message app]
                   "Given a message, take the current app state and
                   return the new one. In essense this is a reducing
                   function."))

(defn process-message [app message]
  (process-message* message app))

(defprotocol EventSource
  (watch-channels [source]))


(def ^:private !dispatch-state (atom {:channels #{}
                                      :dispatch-channel nil}))



(defn- fresh-state [c]
  (let [{:keys [channels dispatch-channel]} @!dispatch-state
        channels (-> channels
                     (disj dispatch-channel)
                     (conj c))]
    {:channels channels
     :dispatch-channel c}))


(defn- remove-closed-channel! [c]
  (swap! !dispatch-state update :channels #(disj % c)))

(defn- apply-message-consequence! [!app-db m]
  (swap! !app-db process-message m))

(defn- add-event-source! [source]
  (swap! !dispatch-state update :channels #(set/union % (watch-channels source))))

(defn start-dispatcher! [!app-db]
  (let [dispatch-c (async/chan)]

    ; set up dispatch state
    (reset! !dispatch-state (fresh-state dispatch-c))

    ; start dispatch loop
    (go-loop
      []
      (when-let [channels (-> !dispatch-state deref :channels seq)]

        (let [[message channel] (async/alts! channels)]
          (cond
            (nil? message)
            (remove-closed-channel! channel)

            (satisfies? Message message)
            (apply-message-consequence! !app-db message)

            (satisfies? EventSource message)
            (add-event-source! message)))
        (recur)))))


(defn dispatch! [message]
  (async/put! (:dispatch-channel @!dispatch-state) message))



