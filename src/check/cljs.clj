(ns check.cljs
  (:require [check.arrows :refer [assert-arrow]]
            [clojure.test :refer [do-report]]))

(defmacro check [left arrow right]
  `(try
     (do-report ~(assert-arrow left arrow right))
     (catch js/Object t#
       (do-report {:type :error
                   :message (str "Expected " (quote ~left) (quote ~arrow) (quote ~right))
                   :expected ~right
                   :actual t#}))))
