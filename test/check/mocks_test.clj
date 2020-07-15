(ns check.mocks-test
  (:require [clojure.test :refer [deftest testing]]
            [check.core :refer [check]]
            [check.mocks :refer [mocking]]))

(defn some-function [a b]
  (+ a b))

(deftest mocking-some-function
  (testing "mocking a single call of a function"
    (mocking
     (some-function 10 20) => 10
     ---
     (check (some-function 10 20) => 10)))

  (testing "error if mock don't agree"
    (mocking
     (some-function 1 2) => 10
     ---
     (check (some-function 0 0) =throws=> clojure.lang.ExceptionInfo))))

(deftest mocking-more-than-one-arg
  (testing "mocking a single call of a function"
    (mocking
     (some-function 10 20) => 10
     (some-function 0 0) => 9
     ---
     (check (some-function 10 20) => 10)
     (check (some-function 0 0) => 9))))

(deftest mocking-side-effects
  (testing "mocking a single call of a function"
    (mocking
     (some-function 10 20) =streams=> [1 2]
     ---
     (check (some-function 10 20) => 1)
     (check (some-function 10 20) => 2)
     (check (some-function 10 20) =throws=> clojure.lang.ExceptionInfo))))
