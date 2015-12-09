# ReactRails in Reagent

Test project to convert http://www.reactrails.com to [reagent](https://reagent-project.github.io/)

Here's the key functionality to support:

## Essentials
* Simple, single table model with no authentication.
* Handling async calls
* Simple validations, server and/or client

## Nice to Have
* Nice to show off some react features
* Deployable to heroku
* Backend of Datomic

Anything else to add to this list?

Please submit PRs!

By the way, if there's an alternative ClojureScript framework that's preferable, let me know and I'll create a separate repo to compare that one.

See also: https://github.com/shakacode/reactrails-in-om-next-example

# cljs-react-on-rails

FIXME: Write a one-line description of your library/project.

## Overview

FIXME: Write a paragraph about the library/project and highlight its goals.

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 
