(defproject check "0.0.3-SNAPSHOT"
  :description "Test helpers"
  :url "https://github.com/mauricioszabo/check"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/core.async "0.4.490"]
                 [net.cgrand/macrovich "0.2.1"]
                 [expectations "2.2.0-rc3"]]

  :profiles {:dev {:src-paths ["dev"]
                   :dependencies [[org.clojure/clojure "1.9.0"]
                                  [thheller/shadow-cljs "2.7.9"]
                                  [org.clojure/clojurescript "1.10.439"]]}})
