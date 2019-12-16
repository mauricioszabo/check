(ns check.core
  (:require [clojure.string :as str]
            [expectations :refer [compare-expr ->failure-message in]]
            [clojure.test :refer [do-report]]
            [net.cgrand.macrovich :as macros]
            [matcher-combinators.core :as c]
            [matcher-combinators.printer :as p]))

(defmulti assert-arrow (fn [cljs? left arrow right] arrow))

(defmethod assert-arrow '=> [_ left _ right]
  `(let [qleft# (quote ~left)
         qright# (quote ~right)
         result# (c/match ~right ~left)
         msg# (->> result# :matcher-combinators.result/value p/as-string
                   (str (pr-str ~left) "\n\nMismatch:\n"))
         pass?# (= :match (:matcher-combinators.result/type result#))]
     {:type (if pass?# :pass :error)
      :expected qright#
      :actual (symbol msg#)}))

(defmethod assert-arrow '=expect=> [_ left _ right]
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
  `(check (in ~left) ~'=expect=> ~right))

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

(defn- prepare-symbol [left ex]
  `(let [qleft# (quote ~left)]
     (try
       {:type :error
        :message (str "Expected " qleft# " to throw error " (quote ~ex))
        :expected (quote ~ex)
        :actual ~left}
       (catch ~ex t#
         {:type :pass}))))

(defn- prepare-coll [left right]
  (let [[ex fun] right]
    `(let [qleft# (quote ~left)
           qright# (quote ~right)]
       (try
         {:type :error
          :message (str "Expected " qleft# " to throw error " (quote ~ex))
          :expected qright#
          :actual ~left}
         (catch ~ex t#
           (when ~fun
             (~fun t#)))))))

(defmethod assert-arrow '=throws=> [cljs? left _ right]
  (assert (or (and (coll? right) (-> right first symbol?))
              (symbol? right)))
  (if (symbol? right)
    (prepare-symbol left right)
    (prepare-coll left right)))

(defmacro defmatcher [name args & body]
  (prn `assert-arrow)
  `(defmethod assert-arrow '~name [cljs?# left# _# right#]
     (let [custom# (fn ~args ~@body)
           res# (custom# right# left#)]
       {:type (if (:pass? res#) :pass :error)
        :expected left#
        :actual (symbol (:failure-message res#))})))
