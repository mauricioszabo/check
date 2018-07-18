(ns check.core-test
  (:require [clojure.string :as str]))

#?(:cljs
   (require '[clojure.test :refer-macros [deftest testing run-tests]]
            '[check.core :refer-macros [check]])
   :clj
   (require '[clojure.test :refer :all]
            '[check.core :refer [check]]))

#?(:clj
   (defn capture-test-out [f]
     (binding [*test-out* (java.io.StringWriter.)]
       (f)
       (str *test-out*)))
   :cljs
   (defn capture-test-out [f]
     (let [out (atom "")]
       (binding [*print-fn* (fn [ & args]
                              (swap! out #(apply str % (str/join " " args) "\n")))]
         (f)
         @out))))

(deftest check-wraps-expect-library
  (check (capture-test-out #(check (inc 10) => 21))
         => #"(?m)expected: 21.*\n.*was: 11"))

#?(:clj
   (deftest check-captures-exceptions
     (check (capture-test-out #(check (/ 10 0) => 0))
            => #"Divide by zero"))

   :cljs
   (deftest check-captures-exceptions
     (check (capture-test-out #(check (js/Error. "Divide by zero") => 0))
            => #"Divide by zero")))

(deftest checks-for-in-behavior
  (check (capture-test-out #(check [1 2 3] =includes=> 4))
         => #"expected: 4.*\n.*was: 1"))

(deftest checks-for-exception
  (check (throw (ex-info "Exception" {:foo "BAR"}))
         =throws=> clojure.lang.ExceptionInfo))

(run-tests)
