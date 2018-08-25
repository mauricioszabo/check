(ns check.core
  (:require [clojure.string :as str]
            [expectations :refer [compare-expr ->failure-message in]]
            [clojure.test :refer [do-report]]))

(defmulti assert-arrow (fn [left qleft arrow right qright] arrow))

(defmethod assert-arrow '=> [left qleft _ right qright]
  (let [result (compare-expr right @left qright qleft)
        unformatted-msg (expectations/->failure-message result)
        msg (str/replace unformatted-msg #"^.*?\n" (str qleft " => " qright))]
    {:type (:type result)
     :message msg
     :expected qright
     :actual qleft}))

(defn check* [left quoted-left arrow right quoted-right]
  (try
    (do-report (assert-arrow left quoted-left arrow right quoted-right))
    (catch #?(:cljs js/Error :clj Throwable) t
      (do-report {:type :error
                  :message (str "Expected " quoted-left " " arrow " " quoted-right)
                  :expected right
                  :actual t}))))

(defmacro check [left arrow right]
  `(check* (delay ~left) (quote ~left) (quote ~arrow) ~right (quote ~right)))

(defmethod assert-arrow '=includes=> [left qleft _ right qright]
  (check* (delay (in @left)) qleft '=> right qright))

(defmethod assert-arrow '=throws=> [left qleft _ right qright]
  (try
    {:type :error
     :message (str "Expected " qleft " to throw error " qright)
     :expected right
     :actual @left}
    (catch #?(:cljs js/Error :clj Throwable) t
      (check* (delay t) qleft '=> right qright))))
