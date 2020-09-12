(ns check.async-test
   (:require [clojure.test :refer [deftest is] :as t]
             [clojure.core.async :as async :refer [>! timeout go <!]]
             [check.async :refer [async-test await! promise-test] :as a]
             [check.core :refer [check]]
             [promesa.core :as p]
             [net.cgrand.macrovich :as macros]))

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

(defn- async-fun []
  (let [c (async/chan)]
    (go
      (async/<! (async/timeout 100))
      (async/>! c "what\nI want\nto check\nreally\n")
      (async/>! c "something else")
      (async/close! c))
    c))

(deftest multi-check-channel
  (async-test "Resolves with regexp"
   (check (async-fun) =resolves=> #"to check")))

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
     (check (await! (. js/Promise resolve 10)) => 10)
     (check (. js/Promise resolve 10) =resolves=> 10))))

(def teardown (atom :initialized))

(deftest tearing-down
  (async-test "will teardown resources" {:teardown (reset! teardown :done)}
    (let [c (async/promise-chan)]
      (reset! teardown :init)
      (go
       (<! (timeout 100))
       (>! c "ok"))
      (check c =resolves=> "ok")))
  #?(:clj (check @teardown => :done)))

(macros/case
 :cljs
 (deftest after-teardown
   (check @teardown => :done)))

(deftest promised-test
  (macros/case
   :cljs
   (promise-test "checking for promises"
     (a/testing "awaits promises"
      (check (. js/Promise resolve 10) => 10)
      (p/delay 100)
      (check (. js/Promise resolve 10) => 10)))))
