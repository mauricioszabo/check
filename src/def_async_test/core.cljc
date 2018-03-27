(ns def-async-test.core
  (:require [clojure.string :as str]))

#?(:cljs
    (ns def-async-test.core
      (:require-macros [cljs.core.async.macros])
      (:require [cljs.core.async :refer [close! chan >! <! alts!]]
                [clojure.test :refer-macros [deftest async testing is]]
                [clojure.test :refer [do-report]]
                [clojure.string :as str])))

#?(:clj
   (require '[expectations :refer [compare-expr ->failure-message in]]
            '[clojure.test :refer [deftest testing do-report is assert-expr]]
            '[clojure.core.async :refer [close! chan >! <! go]]))

(defn cljs-env?
  "Take the &env from a macro, and tell whether we are expanding into cljs."
  [env]
  (boolean (:ns env)))

(defmacro if-cljs
  "Return then if we are generating cljs code and else for Clojure code.
   https://groups.google.com/d/msg/clojurescript/iBY5HaQda4A/w1lAQi9_AwsJ"
  [then else]
  (if (cljs-env? &env) then else))

(defmacro await! [chan]
  `(if-cljs
     (cljs.core.async/<! ~chan)
     (first (clojure.core.async/alts!! [~chan (clojure.core.async/timeout 3000)]))))

(defmacro await-all! [chans]
  `(if-cljs
     (cljs.core.async/alts! ~chans)
     (clojure.core.async/alts!! ~chans)))

(defmacro def-async-test [description opts & cmds]
  (assert (map? opts) "second parameter must be a map")
  (let [norm-desc (symbol (-> description
                              (str/replace #"\s" "-")
                              (str/replace #"[^\-\w\d]" "")))]
    `(if-cljs
       (deftest ~norm-desc
         (cljs.test/async done#
                          (cljs.core.async.macros/go
                           (let [mark-as-done# (delay
                                                ~(if-let [teardown (:teardown opts)]
                                                   teardown)
                                                (done#))]
                             (testing ~description
                               (js/setTimeout (fn []
                                                (when-not (realized? mark-as-done#)
                                                  (cljs.test/is (throw "Async test error - not finalized"))
                                                  @mark-as-done#))
                                              3000)
                               ~@cmds
                               @mark-as-done#)))))
       (deftest ~norm-desc
         (testing ~description
           (try
             ~@cmds
             (finally
               ~(if-let [teardown (:teardown opts)]
                  teardown))))))))

(defmulti assert-arrow (fn [left arrow right] arrow))

(defmethod assert-arrow '=> [left _ right]
  `(let [qleft# (quote ~left)
         qright# (quote ~right)
         result# (compare-expr ~right ~left qright# qleft#)
         unformatted-msg# (->failure-message result#)
         msg# (str/replace unformatted-msg# #"^.*?\n" (str qleft# " => " qright#))]
     {:type (:type result#)
      :message msg#
      :expected qright#
      :actual qleft#}))

(defmethod assert-arrow '=includes=> [left _ right]
  `(check (in ~left) ~'=> ~right))

(defmacro check [left arrow right]
  `(try
     (do-report ~(assert-arrow left arrow right))
     (catch Throwable t#
       (do-report {:type :error
                   :message (str "Expected " (quote ~left) (quote ~arrow) (quote ~right))
                   :expected ~right
                   :actual t#}))))

(macroexpand-1 '(check [1 2 3] =includes=> 4))
