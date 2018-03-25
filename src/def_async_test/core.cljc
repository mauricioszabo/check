#?(:cljs
    (ns def-async-test.core
      (:require-macros [cljs.core.async.macros])
      (:require [cljs.core.async :refer [close! chan >! <! alts!]]
                [clojure.test :refer-macros [deftest async testing do-report is]]
                [clojure.string :as str]))
   :clj
    (ns def-async-test.core
      (:require [clojure.core.async :refer [close! chan >! <! go]]
                [clojure.string :as str])))

#?(:clj
   (require '[expectations :refer [compare-expr ->failure-message]]
            '[clojure.test :refer [deftest testing do-report is assert-expr]]))

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
  (let [result (compare-expr left right left right)
        unformatted-msg (->failure-message result)
        msg (str/replace unformatted-msg #"^.*?\n" (str left " => " right))]
    {:type (:type result)
     :message msg
     :expected left
     :actual right}))

(defmacro check [left arrow right]
  `(let [result# (assert-arrow ~left (quote ~arrow) ~right)]
     (do-report result#)))
     ; result#))

(defmethod assert-expr `assert-arrow [msg form]
  (println form)
  `(let [[left# arrow# right#] ~form
         result# (assert-arrow left# arrow# right#)]
     (do-report result#)
     (-> result# :type (= :pass))))

;
;
(println
 ; (str/replace
  (expectations/->failure-message
   (expectations/compare-expr ["foo" "bar"] ["foo" "baz"] '["foo" "bar"] '["foo" "baz"])))
  ; #"^.*?\n" "")
;
(require '[expectations :refer [expect]]
         '[clojure.string :as str])
; (expect ["foo" "bar"] ["foo" "baz"])
; (expectations/run-all-tests)
