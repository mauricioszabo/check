(defproject check "0.1.0"
  :description "Test helpers"
  :url "https://github.com/mauricioszabo/check"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/core.async "1.2.603"]
                 [net.cgrand/macrovich "0.2.1"]
                 [funcool/promesa "4.0.2"]
                 [nubank/matcher-combinators "2.1.0"]
                 [expectations "2.2.0-rc3"]]

  :profiles {:dev {:src-paths ["dev"]
                   :dependencies [[org.clojure/clojure "1.10.0"]
                                  [thheller/shadow-cljs "2.10.10"]]}}
  :repl-options {:init-ns user})
