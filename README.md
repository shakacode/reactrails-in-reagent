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



## Running the project
### Setting up environment variables
You can set the port the web server will listen to. The app uses [environ](https://github.com/weavejester/environ) 
and so you can create the file `.lein-env' at the root of the project. The file should look like this:
```clojure
{:port "9000"}
```

### Installing dependencies
The app uses [leiningen](http://leiningen.org) and [npm](https://www.npmjs.com) to manage dependencies. 
You need to `npm install` to get javascript third party libraries.

### Running the production app
In a terminal, after installing with npm:

1. `lein uberjar`
2. `java -jar target/reactrails-in-reagent-standalone.jar `

### Development 
The following explains how to quickly get the backend and the frontend running in development.
To see the set up take a look at the file `src/dev/user.clj`.

#### Backend
To start the backend in development start a repl with `lein repl` then `(go)`.

#### Frontend
[Figwheel](https://github.com/bhauman/lein-figwheel) provides auto compiling 
of the cljs sources and live reload. To start figwheel, in a terminal:

1. `lein repl`
2. `(start-figwheel! "dev")`
3. Open your browser to `localhost:PORT/`

#### Devcards and Gorilla worksheets
The [Gorilla REPL](http://gorilla-repl.org) plugin is used to create some interactive documentation of the backend.
Worksheets are in the `ws` directory. To start gorilla, in a terminal:

1. `lein gorilla`
2. open your browser at the url provided.

[Devcards](https://github.com/bhauman/devcards) is used in the frontend for the same purpose. 
To start devcards, in a terminal:

1. `lein repl`
2. `(start-figwheel! "devcards")`
3. open your browser to `localhost:PORT/devcards`

#### Other considerations
The project is setup using figwheel-sidecar instead of the lein-figwheel plugin. This allows for easier 
integration with editors, no need for a [special repl config](https://github.com/bhauman/lein-figwheel/wiki/Running-figwheel-in-a-Cursive-Clojure-REPL) 
in [Cursive](https://cursive-ide.com) for instance.

This [page](http://gorilla-repl.org/editors.html) of the Gorilla REPL website explains how to connect with your text editor.

## Running the tests
For the backend tests just `lein test` in a terminal.

[Karma](https://karma-runner.github.io/0.13/index.html) is used to run the frontend tests.
After [installing the karma client](https://karma-runner.github.io/0.13/intro/installation.html), 
you can run the frontend tests in a terminal:

1. `lein cljsbuild once test`
2. `karma start`

You can also run the tests on the minified app (ClojureScript compiler `:optimizations :advanced`)

1. `lein cljsbuild once test-min`
2. `karma start karma.conf-min.js`

The tests are also available as devcards.