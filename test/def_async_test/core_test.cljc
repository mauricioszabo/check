(ns def-async-test.core-test)

#?(:cljs (require-macros '[cljs.core.async.macros :refer [go]]))

#?(:clj
   (require '[clojure.test :refer [is run-tests]]
            '[clojure.core.async :refer [chan >! timeout go <!]]
            '[def-async-test.core :refer [def-async-test await! check]])
   :cljs
   (require '[clojure.test :refer-macros [is testing run-tests async deftest]]
            '[cljs.core.async :refer [chan >! timeout <!]]
            '[def-async-test.core :refer-macros [def-async-test await! check]]))

(def-async-test "when things run correctly" {}
  (is (= 1 1)))

(def-async-test "when there's a async test" {}
  (let [c (chan)]
    (go
     (<! (timeout 200))
     (>! c "ok"))
    (is (= "ok" (await! c)))))

(def collateral (atom :resetted))
(def-async-test "validates some collateral damage"
  {:teardown (reset! collateral :resetted)}
  (swap! collateral name)
  (is (= @collateral "resetted")))

(def-async-test "validates that teardown works" {}
  (is (= @collateral :resetted)))

(defn capture-test-out [f]
  (binding [clojure.test/*test-out* (java.io.StringWriter.)]
    (f)
    (str clojure.test/*test-out*)))

(def-async-test "wraps `assert` library" {}
  (check (capture-test-out #(check (inc 10) => 21))
         => #"(?m)expected: 21.*\n.*was: 11")

  (check (capture-test-out #(check (/ 10 0) => 0))
         => #"Divide by zero"))

(def-async-test "checks for `in` behavior" {}
  (check (capture-test-out #(check [1 2 3] =includes=> 4))
         => #"expected: 4.*\n.*was: 1"))

(run-tests)
