(ns check.arrows
  (:require [clojure.string :as str]
            [expectations :refer [compare-expr ->failure-message in]]))

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

; (defmethod assert-arrow '=includes=> [left _ right]
;   `(check (in ~left) ~'=> ~right))
