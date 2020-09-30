(ns check.async
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [check.core :as core]
            [clojure.test :as test]
            [clojure.core.async :as async]
            [promesa.core :as p]
            [clojure.walk :as walk]
            [net.cgrand.macrovich :as macros]
            [clojure.core.async.impl.protocols :as proto])
  (:import [clojure.core.async.impl.protocols ReadPort]))

(def ^:dynamic timeout 3000)
; (defmacro await-all! [chans]
;   (macros/case
;     :cljs `(async/alts! ~chans)
;     :clj `(async/alts!! ~chans)))
;
; (defn- to-chan [left]
;   `(let [chan# (async/promise-chan)]
;        (.then ~left (fn [result#] (async/put! chan# result#)))
;        chan#))
;
(defn- get-from-channel! [chan]
  `(cond
     (p/promise? ~chan) (async/go @~chan)
     (instance? ReadPort ~chan) ~chan
     :else (async/go ~chan)))
  ; (if cljs?
  ;  `(async/<! (if (instance? js/Promise ~chan)
  ;               ~(to-chan chan)
  ;               ~chan))
  ;  `(first (async/alts!! [~chan (async/timeout timeout)]))))

(defmacro await! [chan]
  (macros/case
   :cljs `(to-promise ~chan)
   :clj `(async/<!! ~(get-from-channel! chan))))

; (defn- wrap-in-prom [body]
;   (let [resolve (fn [[check left arrow right]]
;                   (let [sym (gensym "res")]
;                     `(p/let [~sym ~left]
;                        (~check ~sym ~arrow ~right))))]
;     (walk/postwalk
;      #(cond-> %
;               (and (list? %) (or (= 'check (first %))
;                                  (str/ends-with? (-> % first str) "/check")))
;               resolve)
;      body)))
;
(defmacro testing [description & body]
  (macros/case
   :clj `(test/testing ~description ~@body)
   :cljs `(test/testing ~description (p/do! ~@body))))

(defn async-test* [description timeout teardown-delay go-thread]
  (test/testing description
    (let [[res] (async/alts!! [go-thread
                               (async/thread (async/<!! (async/timeout timeout)) ::timeout)]
                             :priority true)]
      (case res
        ::ok @teardown-delay
        ::timeout (do
                    @teardown-delay
                    (throw (ex-info "Async error - not finalized" {:timeout timeout})))
        (do
          @teardown-delay
          (throw res))))))

(s/def ::description string?)
(s/def ::teardown fn?)
(s/def ::timeout int?)
(s/def ::params map? #_(s/keys :opt-un [::timeout ::teardown]))
(s/def ::body any?)
(s/def ::full-params
  (s/cat :description ::description
         :params (s/? ::params)
         :body (s/* ::body)))

(defmacro async-test [ & params]
  (let [{:keys [description params body]} (s/conform ::full-params params)
        timeout (:timeout params 2000)
        teardown (:teardown params nil)]
        ; done-gen (gensym "done-")]
        ; done]
    (macros/case
     :clj `(async-test* ~description ~timeout
             (delay ~teardown)
             (async/thread (try ~@body ::ok (catch Throwable t# t#))))
     :cljs `(async-test* ~description ~timeout (fn [] ~teardown) ~body))))
(s/fdef async-test :args (s/cat :params ::full-params))

(defmacro check [left arrow right]
  (let [cljs? (macros/case :cljs true :clj false)
        lft (gensym "left-")
        rgt (gensym "right-")]
    (macros/case
     :clj `(let [~lft (await! ~left)
                 ~rgt (await! ~right)]
             (test/do-report ~(core/assert-arrow cljs? lft arrow rgt)))
     :cljs `(p/let [~lft (await! ~left) ~rgt (await! ~right)]
              (test/do-report ~(core/assert-arrow cljs? lft arrow rgt))
              :done))))
