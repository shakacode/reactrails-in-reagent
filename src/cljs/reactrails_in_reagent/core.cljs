(ns reactrails-in-reagent.core
  (:require
    [reagent.core :as r]
    [reactrails-in-reagent.dispatch :as d]
    [reactrails-in-reagent.views :as views]
    [reactrails-in-reagent.actions :as actions]
    [cljs.core.async :as async :refer [<!]])
  (:require-macros
    [cljs.core.async.macros :refer [go go-loop alt!]]))

(enable-console-print!)


(defn compare-dates [comment1 comment2]
  (->> [comment1 comment2]
       (map (juxt :comment/created :db/id))
       reverse
       (apply compare)))


(def initial-state {:nav/index 0
                    :comments (sorted-set-by compare-dates)})

(defonce app-state (r/atom initial-state))


(defonce fetcher-control (atom (async/chan)))

(defn spawn-fetcher! [period]
  (let [done? (atom false)
        control-chan (async/chan)]
    (go (while (not @done?)
          (let [t-out (async/timeout period)]
            (alt!
              control-chan (reset! done? true)
              t-out (do (println "refreshing comments")
                        (d/dispatch! (actions/->GetAllComments)))))))
    control-chan))


(defn start-fetching! [period]
  (swap! fetcher-control async/close!)
  (reset! fetcher-control (spawn-fetcher! period)))



(defn main [& args]
  (reset! app-state initial-state)
  (views/render! app-state)
  (d/start-dispatcher! app-state)
  (d/dispatch! (actions/->GetAllComments))
  (start-fetching! 10000)
  )


(defonce started (main))


(defn on-js-reload []
  (.log js/console "reloaded!!!")
  (views/render! app-state)
  )

