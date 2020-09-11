(ns check.mocks
  (:require [clojure.spec.alpha :as s]))

(s/def ::arrow '#{=> =streams=>})
(s/def ::template (s/cat :fn symbol? :args (s/* any?)))
(s/def ::mocks (s/cat
                :mocks (s/+ (s/cat :template (s/spec ::template) :arrow ::arrow :return any?))
                :arrow '#{--- ===}
                :body (s/* any?)))

(defn- normalize-return [{:keys [arrow fn args return]}]
  (case arrow
    => {:return return}
    =streams=> (let [s (gensym "stream-")]
                 {:let-fn `[~s (atom ~return)]
                  :fn `(fn []
                         (when (empty? @~s)
                           (throw (ex-info "No more values to stream on mock"
                                           {:function '~fn
                                            :args ~args})))
                         (let [ret# (first @~s)]
                           (swap! ~s rest)
                           ret#))})))

(defn- normalize-mocking-params [mockings]
  (->> mockings
       (map (fn [{:keys [template return arrow]}]
              [(:fn template) (assoc template :arrow arrow :return return)]))
       (group-by first)
       (map (fn [[k v]]
              [k (->> v
                      (map (fn [[_ v]] [(:args v) (normalize-return v)]))
                      (into {}))]))))
       ; (into {})))

(defn- to-function [[fun args+return]]
  (let [all-lets (->> args+return
                      (map (comp :let-fn second))
                      (filter identity)
                      (mapcat identity))]

    [fun
     `(let [~@all-lets]
        (fn ~fun [ & old-args#]
          (if-let [return# (get ~args+return old-args#)]
            (let [{:keys [~'fn ~'return]} return#]
              (cond
                ~'fn (~'fn)
                ~'return ~'return))
            (throw (ex-info "No mocked calls for this fn/args"
                            {:function '~fun
                             :expected-args (keys ~args+return)
                             :actual-args old-args#})))))]))

(defmacro mocking
  "Mocks a group of calls. "
  [ & args]
  (s/assert* ::mocks args)
  (let [{:keys [mocks body]} (s/conform ::mocks args)
        mockings (->> mocks
                      normalize-mocking-params
                      (mapcat to-function)
                      vec)]
    `(with-redefs ~mockings
       ~@body)))
