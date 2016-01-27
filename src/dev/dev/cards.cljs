(ns dev.cards
  (:require
    [reactrails-in-reagent.dispatch :as dispatch]
    [reactrails-in-reagent.views :as views]
    [reactrails-in-reagent.actions :as actions]

    [reactrails-in-reagent.actions-test]
    [reactrails-in-reagent.dispatch-test])
  (:require-macros
    [devcards.core :as dc :refer [defcard defcard-doc]]))

(defcard-doc
  "# ReactRails in Reagent

  ## Architecture
  The architecture of the app is similar to what we could find with a [redux](https://github.com/rackt/redux) app
  with its 3 principles:

  * the 'read only state' property since we use clojure's atoms
  * a single source of truce since all the app state is kept a single atom
  * We have actions that are applied as pure functions from one immutable
  value of the state to the next immutable value of the state.


  ### The dispatcher
  The engine of the app is the dispatcher inspired by [Petrol](https://github.com/krisajenkins/petrol)
  found in `reactrails-in-reagent.dispatch`.

  We have a dispatch function"
  (dc/mkdn-pprint-source dispatch/dispatch!)

  "
  that is used to dispatch any event onto the app's dispatch loop. For instance take a look at
  the view used to select the form's style:"
  (dc/mkdn-pprint-source views/form-style-selector)

  "
  We can see that each click on a link will dispatch a `->SelectFormStyle` action.

  We can also take a look at one of the form views:"
  (dc/mkdn-pprint-source views/template-form-inline)

  "
  and the submit function:"
  (dc/mkdn-pprint-source views/submit-fn)

  "
  ### Actions & EventSources
  There are two types of events we can dispatch onto the dispatch loop: Actions and EventSources.

  #### Actions
  Actions are implemented as clojure records in the `reactrails-in-reagent.action` namespace and
  satisfy the `reactrails-in-reagent.dispatch/Action` protocol. They are what is _reduced_ on the app state."
  (dc/mkdn-pprint-source dispatch/Action)
  (dc/mkdn-pprint-source dispatch/apply-action)
  "
  We can take a look at the implementation of an action:"
  (dc/mkdn-pprint-source actions/SelectFormStyle)

  "
  More informations in the reactrails\\_in\\_reagent.actions_test devcard.

  #### Eventsources
  EventSource are used to model actions that necessitate an asynchronous operation.
  They are records in the `reactrails-in-reagent.action` namespace that implement
  the `reactrails-in-reagent.dispatch/EventSource` protocol."
  (dc/mkdn-pprint-source dispatch/EventSource)

  "Here is the implementation of the action dispatched by a form view:"
  (dc/mkdn-pprint-source actions/NewComment)

  "In this case the event loop will store `channel-res`in a set of pending channels.
  The event source also executes a http call to the server and the result will be put `channel-res`,
  int the form of a `reactrails-in-reagent.actions/ReceivedComment`."
  )