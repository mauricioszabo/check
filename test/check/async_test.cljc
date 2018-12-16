(ns check.async-test
   (:require [clojure.test :refer [run-tests is] :include-macros true]
             [clojure.core.async :refer [chan >! timeout go <!] :include-macros true]
             [check.async :refer [def-async-test await!] :include-macros true]
             [check.core :refer [check] :include-macros true]))

(def-async-test "when things run correctly" {}
  (is (= 1 1)))

(def-async-test "when there's a async test" {}
  (let [c (chan)]
    (go
     (<! (timeout 200))
     (>! c "ok"))
    (is (= "ok" (await! c)))))

(def-async-test "when running with check" {}
  (let [c (chan)]
    (go
     (<! (timeout 200))
     (>! c "ok"))
    (check (await! c) => "ok")))
