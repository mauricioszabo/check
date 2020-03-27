(ns check.core-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest testing run-tests] :as t]
            [check.core :refer [check] :as check :include-macros true]))

(deftest check-wraps-matcher-combinators
  (testing "simple checks"
    (check {:foo 12} => {:foo 12}))

  (testing "regexp checks"
    (check (str 10) => #"\d\d")))

(deftest matcher-combinators
  (testing "implements code to check strings"
    (check "foobar is a string" => "foobar is a string")))

(deftest check-captures-exceptions
  (testing "checks only for exception type"
    (check (throw (ex-info "Wow, some error!" {}))
           =throws=> #?(:clj clojure.lang.ExceptionInfo
                        :cljs cljs.core.ExceptionInfo)))

  (testing "checks for exception type, and checks more"
    (check (throw (ex-info "Wow, some error!" {}))
           =throws=> [#?(:clj clojure.lang.ExceptionInfo
                         :cljs cljs.core.ExceptionInfo)
                      #(check #?(:clj (.getMessage %)
                                 :cljs (.-message %))
                              => "Wow, some error!")])))

(deftest checks-for-in-behavior
  (check [1 2 3] =includes=> 2))

; CUSTOM MATCHERS
(check/defmatcher is-the-same? [expected actual]
  {:pass? (identical? expected actual)
   :failure-message "They are not the same object!"})

(deftest custom-matcher
  (let [obj #?(:cljs (js/Object.) :clj (Object.))]
    (check obj is-the-same? obj)))

(defn stateful-obj []
  (let [a (atom [])]
    (fn [x]
      (swap! a conj (inc x))
      @a)))

(deftest stateful-matchers
  (let [add! (stateful-obj)]
    (check (add! 10) => [11])
    (check (add! 11) => [11 12])
    (check (add! 12) => [11 12 13])))
