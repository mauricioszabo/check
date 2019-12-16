(ns check.core-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest testing] :as t]
            [matcher-combinators.test]
            [check.core :refer [check] :as check :include-macros true]))

(deftest check-wraps-matcher-combinators
  (testing "simple checks"
    (check {:foo 12} => {:foo 12}))

  (testing "regexp checks"
    (check (str 10) => #"\d\d")))

(deftest check-captures-exceptions
  (testing "checks only for exception type"
    (check (throw (ex-info "Wow, some error!" {}))
           =throws=> cljs.core.ExceptionInfo))

  (testing "checks for exception type, and checks more"
    (check (throw (ex-info "Wow, some error!" {}))
           =throws=> [cljs.core.ExceptionInfo
                      #(check (.-message %) => "Wow, some error!")])))

(deftest checks-for-in-behavior
  (check [1 2 3] =includes=> 2))

; CUSTOM MATCHERS
(check/defmatcher is-the-same? [expected actual]
  {:pass? (identical? expected actual)
   :failure-message "They are not the same object!"})

(macroexpand-1 '
               (check/defmatcher is-the-same? [expected actual]
                 {:pass? (identical? expected actual)
                  :failure-message "They are not the same object!"}))

(clojure.core/defmethod
  check.core/assert-arrow 'is-the-same?
  [cljs?__55487__auto__ left__55488__auto__ ___55489__auto__ right__55490__auto__]
  (clojure.core/let [custom__55491__auto__ (clojure.core/fn [expected actual] {:pass? (identical? expected actual), :failure-message "They are not the same object!"})
                     res__55492__auto__ (custom__55491__auto__ right__55490__auto__ left__55488__auto__)]
    {:type (if (:pass? res__55492__auto__) :pass :error), :expected left__55488__auto__, :actual (clojure.core/symbol (:failure-message res__55492__auto__))}))
#_
(deftest custom-matcher
  (let [obj (js/Object.)]
    (check obj is-the-same? obj)))
