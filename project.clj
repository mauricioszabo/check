(defproject def-async-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.908"]
                 [org.clojure/core.async "0.3.443"]
                 [expectations "2.2.0-rc3"]]

  :profiles {:dev {:src-paths ["dev"]
                   :dependencies [[figwheel-sidecar "0.5.13"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :plugins [[lein-midje "3.2.1"]]}}

  :plugins [[lein-cljsbuild "1.1.7"]]

  :cljsbuild {:builds [{:source-paths ["src" "test"]
                        :id "test"
                        :compiler {:output-to "target/test.js"
                                   :optimizations :simple
                                   :hashbang false
                                   :language-in :ecmascript5
                                   :output-wrapper true
                                   :output-dir "target/js-test"
                                   :pretty-print true
                                   :target :nodejs}}]})
                       ; {:source-paths ["src" "test"]
                       ;  :id "dev"
                       ;  :figwheel true
                       ;  :compiler {:output-to "target/dev.js"
                       ;             :output-dir "target/js"
                       ;             :main microscope.rabbit.all-tests
                       ;             :optimizations :none
                       ;             :target :nodejs}}]})
