(ns ^:figwheel-no-load reactrails-in-reagent.app
  (:require [reactrails-in-reagent.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/set-pref! :install-sanity-hints true)
(devtools/install!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback (fn [] (core/mount-components)))

(core/init!)
