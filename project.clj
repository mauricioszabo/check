(defproject check "0.0.1-SNAPSHOT"
  :description "Test helpers"
  :url "https://github.com/mauricioszabo/check"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/core.async "0.4.474"]
                 [expectations "2.2.0-rc3"]]

  :profiles {:dev {:src-paths ["dev"]
                   :dependencies [[org.clojure/clojure "1.8.0"]
                                  [org.clojure/clojurescript "1.10.238"]
                                  [figwheel-sidecar "0.5.13"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :plugins [[lein-midje "3.2.1"]]}}

  :plugins [[lein-cljsbuild "1.1.7"]]

  :cljsbuild {:builds [{:source-paths ["src" "test"]
                        :id "dev"
                        :figwheel true
                        :compiler {:output-to "target/dev.js"
                                   :output-dir "target/js"
                                   :main check.all-tests
                                   :optimizations :none
                                   :warnings {:single-segment-namespace false}
                                   :target :nodejs}}
                       {:source-paths ["src" "test"]
                        :id "test"
                        :compiler {:output-to "target/test.js"
                                   :output-dir "target/test-js"
                                   :output-wrapper true
                                   :hashbang false
                                   :pretty-print true
                                   :optimizations :simple
                                   :warnings {:single-segment-namespace false}
                                   :target :nodejs}}]})
