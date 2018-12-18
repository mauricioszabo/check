(ns check.core-test
  (:require [clojure.string :as str]
            [clojure.test :as t :include-macros true]
            [check.core :refer [check] :include-macros true :as c]))

#?(:clj
   (defn capture-test-out [f]
     (binding [t/*test-out* (java.io.StringWriter.)]
       (f)
       (str t/*test-out*)))
   :cljs
   (defn capture-test-out [f]
     (let [out (atom "")]
       (binding [*print-fn* (fn [ & args]
                              (swap! out #(apply str % (str/join " " args) "\n")))]
         (f)
         @out))))

(t/deftest check-wraps-expect-library
  (check (capture-test-out #(check (inc 10) => 21))
         => #"(?m)expected: 21.*\n.*was: 11"))

#?(:clj
   (t/deftest check-captures-exceptions
     (check (capture-test-out #(check (/ 10 0) => 0))
            => #"Divide by zero"))

   :cljs
   (t/deftest check-captures-exceptions
     (check (capture-test-out #(check (js/Error. "Divide by zero") => 0))
            => #"Divide by zero")))

(t/deftest checks-for-in-behavior
  (check (capture-test-out #(check [1 2 3] =includes=> 4))
         => #"expected: 4.*\n.*was: 1"))

(t/deftest checks-for-exception
  (check (throw (ex-info "Exception" {:foo "BAR"}))
         =throws=> #?(:cljs cljs.core.ExceptionInfo
                      :clj clojure.lang.ExceptionInfo)))
