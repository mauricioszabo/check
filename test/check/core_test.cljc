(ns check.core-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest testing] :as t]
            [matcher-combinators.test]
            [check.core :refer [check] :include-macros true :as c]))

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
