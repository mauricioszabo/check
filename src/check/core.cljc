(ns check.core
  (:require [clojure.string :as str]
            [expectations :refer [compare-expr ->failure-message in]]
            [clojure.test :refer [do-report]]
            [net.cgrand.macrovich :as macros]))

(defmulti assert-arrow (fn [cljs? left arrow right] arrow))

(defmethod assert-arrow '=> [_ left _ right]
  `(let [qleft# (quote ~left)
         qright# (quote ~right)
         result# (compare-expr ~right ~left qright# qleft#)
         unformatted-msg# (expectations/->failure-message result#)
         msg# (str/replace unformatted-msg# #"^.*?\n" (str qleft# " => " qright#))]
     {:type (:type result#)
      :message msg#
      :expected qright#
      :actual qleft#}))

(defmethod assert-arrow '=includes=> [_ left _ right]
  `(check (in ~left) ~'=> ~right))

(defmacro check [left arrow right]
  (macros/case
   :clj
   `(try
      (do-report ~(assert-arrow false left arrow right))
      (catch java.lang.Throwable t#
        (do-report {:type :error
                    :message (str "Expected " (quote ~left) (quote ~arrow) (quote ~right))
                    :expected ~right
                    :actual t#})))
   :cljs
   `(try
      (do-report ~(assert-arrow true left arrow right))
      (catch js/Object t#
        (do-report {:type :error
                    :message (str "Expected " (quote ~left) (quote ~arrow) (quote ~right))
                    :expected ~right
                    :actual t#})))))

(defmethod assert-arrow '=throws=> [cljs? left _ right]
   `(let [qleft# (quote ~left)
          qright# (quote ~right)]
      (try
        {:type :error
         :message (str "Expected " qleft# " to throw error " qright#)
         :expected ~right
         :actual ~left}
        (catch ~(if cljs? 'js/Object 'java.lang.Throwable) t#
          (check t# ~'=> ~right)))))
