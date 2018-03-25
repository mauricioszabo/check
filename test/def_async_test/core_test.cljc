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

(def-async-test "wraps `assert` library" {}
  ; (clojure.test/do-report {:type :pass :message "Failed"})

  (check (inc 10) => 21))

(run-tests)

(macroexpand '
               (clojure.test/deftest foobar
                 (check 10 => 20)))
(macroexpand-1 '(check 10 => 20))
               ; (def-async-test "wraps `assert` library" {}
               ;   (check 10 => 20)))

(macroexpand '
               (clojure.test/is
                (expectations/compare-expr 10 20 (quote 10) (quote 20))))
