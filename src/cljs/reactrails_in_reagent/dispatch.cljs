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
  (println "removing channel")
  (swap! !dispatch-state update :channels #(disj % c)))

(defn- apply-message-consequence! [!app-db m]
  (println "applyong message")
  (swap! !app-db process-message m))

(defn- add-event-source! [source]
  (println "adding event source")
  (swap! !dispatch-state update :channels #(set/union % (watch-channels source))))

(defn start-dispatcher! [!app-db]
  (let [dispatch-c (async/chan)]

    ; set up dispatch state
    (reset! !dispatch-state (fresh-state dispatch-c))

    ; start dispatch loop
    (go-loop
      []
      (println "enter the loop")
      (when-let [channels (-> !dispatch-state deref :channels seq)]
        (println "apparently we got channels")
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



(comment

  (def app (atom 0))

  (def app-history (atom []))

  (add-watch app :toto (fn [& args] (swap! app-history conj  args)))



  (defrecord Inc []
    Message
    (process-message* [this app]
      (inc app)))

  (defrecord IncLater []
    EventSource
    (watch-channels [this]
      (let [channel-respone (go (->Inc))]
        #{channel-respone})))


  (start-dispatcher! app)


  @app

  @app-history

  @!dispatch-state



  (dispatch! (->Inc))

  (dispatch! (->IncLater))





  )


