(ns check.async-test)

#?(:cljs (require-macros '[cljs.core.async.macros :refer [go]]))

#?(:cljs
   (require '[clojure.test :refer-macros [run-tests is]]
            '[cljs.core.async :refer [chan >! timeout <!]]
            '[check.async :refer-macros [def-async-test] :refer [await!]])
   :clj
   (require '[clojure.test :refer [run-tests is]]
            '[clojure.core.async :refer [chan >! timeout go <!]]
            '[check.async :refer [def-async-test await!]]))

(def-async-test "when things run correctly" {}
  (is (= 1 1)))

(def-async-test "when there's a async test" {}
  (let [c (chan)]
    (go
     (<! (timeout 200))
     (>! c "ok"))
    (is (= "ok" (await! c)))))

(run-tests)
