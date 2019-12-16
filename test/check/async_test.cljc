(ns check.async-test
   (:require [clojure.test :refer [deftest is]]
             [clojure.core.async :as async :refer [>! timeout go <!]]
             [check.async :refer [async-test await!] :include-macros true]
             [check.core :refer [check] :include-macros true]
             [net.cgrand.macrovich :as macros]
             [clojure.pprint :as pp]))


(deftest things-running
  (async-test "when things run correctly"
    (is (= 1 1))))

(deftest some-async-test
  (async-test "when there's a async test"
    (let [c (async/promise-chan)]
      (go
       (<! (timeout 200))
       (>! c "ok"))
      (is (= "ok" (await! c))))))

(deftest checking
  (async-test "when running with check"
    (let [c (async/promise-chan)]
      (go
       (<! (timeout 200))
       (>! c "ok"))
      (check (await! c) => "ok")
      (check c =resolves=> "ok"))))

(macros/case
 :cljs
 (deftest promises
   (async-test "adds a checker for promises"
     (check (. js/Promise resolve 10) =resolves=> 10))))

(def teardown (atom :initialized))

(deftest tearing-down
  (async-test "will teardown resources" {:teardown (reset! teardown :done)}
    (let [c (async/promise-chan)]
      (reset! teardown :init)
      (go
       (<! (timeout 100))
       (>! c "ok"))
      (check c =resolves=> "ok"))))

(deftest after-teardown
  (check @teardown => :done))
