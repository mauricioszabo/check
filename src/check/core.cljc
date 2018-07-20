(ns check.core
  (:require [clojure.string :as str]
            [expectations :refer [compare-expr ->failure-message in]]
            [clojure.test :refer [do-report]]))

#?(:cljs
   (require-macros '[check.utils :refer [if-cljs]])
   :clj
   (require '[check.utils :refer [if-cljs]]))

(defmulti assert-arrow (fn [left qleft arrow right qright] arrow))

(defmethod assert-arrow '=> [fn-left qleft _ right qright]
  (let [result (compare-expr right (fn-left) qright qleft)
        unformatted-msg (expectations/->failure-message result)
        msg (str/replace unformatted-msg #"^.*?\n" (str qleft " => " qright))]
    {:type (:type result)
     :message msg
     :expected qright
     :actual qleft}))

(defn check* [fn-left quoted-left arrow right quoted-right]
  (try
    (do-report (assert-arrow fn-left quoted-left arrow right quoted-right))
    (catch #?(:cljs js/Error :clj Throwable) t
      (do-report {:type :error
                  :message (str "Expected " quoted-left " " arrow " " quoted-right)
                  :expected right
                  :actual t}))))

(defmacro check [left arrow right]
  `(check* (fn [] ~left) (quote ~left) (quote ~arrow) ~right (quote ~right)))

(defmethod assert-arrow '=includes=> [fn-left qleft _ right qright]
  (check* #(in (fn-left)) qleft '=> right qright))

(defmethod assert-arrow '=throws=> [fn-left qleft _ right qright]
  (try
    (let [res (fn-left)]
      {:type :error
       :message (str "Expected " qleft " to throw error " qright)
       :expected right
       :actual res})
    (catch #?(:cljs js/Error :clj Throwable) t
      (check* (constantly t) qleft '=> right qright))))
