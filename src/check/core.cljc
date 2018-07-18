(ns check.core
  (:require [clojure.string :as str]
            [expectations :refer [compare-expr ->failure-message in]]
            [clojure.test :refer [do-report]]))

#?(:cljs
   (require-macros '[check.utils :refer [if-cljs]])
   :clj
   (require '[check.utils :refer [if-cljs]]))

(defmulti assert-arrow (fn [left arrow right] arrow))

(defmethod assert-arrow '=> [left _ right]
  `(let [qleft# (quote ~left)
         qright# (quote ~right)
         result# (compare-expr ~right ~left qright# qleft#)
         unformatted-msg# (expectations/->failure-message result#)
         msg# (str/replace unformatted-msg# #"^.*?\n" (str qleft# " => " qright#))]
     {:type (:type result#)
      :message msg#
      :expected qright#
      :actual qleft#}))

(defmethod assert-arrow '=includes=> [left _ right]
  `(check (in ~left) ~'=> ~right))

(def ^:private root-exception
  (if-cljs 'js/Object 'Throwable))

(defmethod assert-arrow '=throws=> [left _ right]
  `(try
     (let [res# ~left]
       {:type :error
        :message (str "Expected " (quote ~left) " to throw error " (quote ~right))
        :expected ~right
        :actual res#})
     (catch ~root-exception t#
       (check t# ~'=> ~right))))

(defmacro check [left arrow right]
  (if-cljs
    `(try
       (do-report ~(assert-arrow left arrow right))
       (catch js/Object t#
         (do-report {:type :error
                     :message (str "Expected " (quote ~left) (quote ~arrow) (quote ~right))
                     :expected ~right
                     :actual t#})))

    `(try
       (do-report ~(assert-arrow left arrow right))
       (catch Throwable t#
         (do-report {:type :error
                     :message (str "Expected " (quote ~left) (quote ~arrow) (quote ~right))
                     :expected ~right
                     :actual t#})))))
