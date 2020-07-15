# Check

Library helpers for a better testing world.

## Motivation
Clojure's default test library is... well... limited, to say the least. There are better options out there, but `expectations` is too opinated in "one assertion per test", `midje` is **way too magic** (and also don't work on ClojureScript), `speclj` is probably dead (and don't work with async tests).

So, enters this library: it wraps `expectations` and `matcher-combinators` so we're not trying to reinvent the wheel, uses `core.async` to transform simulate synchronous code on ClojureScript async tests, and uses "midje-style" arrows. Everybody wins!

## Usage

You can use the "arrow" expectations the same way you would use `matcher-combinators` lib:

```clojure
(require '[check.core :refer [check]]
         '[clojure.test :refer [deftest]])

(deftest ten-is-even
  (check 10 => even?))

(deftest regexp-test
  (check "some string" => #"str"))

;; You can use `in` like you would use in expectations...
(require '[expectations :refer [in]])
(deftest in-vector
  (check (in [1 2 3 4]) => 3))

;; Or, to avoid another require, you can use =includes=>
(deftest in-with-includes
  (check [1 2 3 4] =includes=> 3))
```

### Async tests

You can use `async-test` to generate a test that will timeout after a while
```clojure
(require '[check.async :refer [async-test await!]]
         '[clojure.core.async :refer [chan >! timeout go <!]])

(deftest some-test
  (async-test "checks for async code"
    (let [c (chan)]
      (future
        (Thread/sleep 400)
        (>! c :done))

      ; Both do the same thing:
      (check (await! c) => :done)
      (check c =resolves=> :done))))
```

## Extending
Suppose you have a very complicated map that represents some internal structure of your code. You probably don't want to keep repeating your map data all over the place, and the matchers that are included are not sufficient. `check` allows you to create custom matchers, so you have a custom way of matching your data to your expectation, and also a custom error to guide you to find the problem in your code.

For example, suppose you have a map in the format:
```clojure
{:accounts [{:name "Savings" :amount 200M}
            {:name "Primary" :amount 20M}]}
```

And you want to check the amount a person have in each account, but you don't want to "tie" your implementation to your "map shape". You can write a checker like this:

```clojure
;; First we define a way to compare what we expect with our current data:
(defn- mismatches [accounts [name amount]]
  (if-let [acc (->> accounts
                    (filter #(-> % :name (= name)))
                    first)]
    (if (= amount (:amount acc))
      nil
      (str "Account " name " should have " amount ", but have " (:amount acc)))
    (str "Account " name " isn't present on list of accounts")))

;; Then we define our matcher, that will call this function to check for problems:
(check/defmatcher =have-amount=> [expected actual]
  (let [accs (:accounts actual)
        misses (map #(mismatches accs %) expected)
        ;; We remove rows where there's no problem at all
        misses (remove nil? misses)]
    {:pass? (empty? misses) ;; If pass? is true, failure-message is not needed
     :failure-message (str "\n" (clojure.string/join "\n" misses))}))

;; Then, on some test:
(deftest check-accounts
  (check {:accounts [{:name "Savings" :amount 200M}
                     {:name "Primary" :amount 20M}]}
         =have-amount=> {"Savings" 100M
                         "Investments" 1000M}))

;; This will fail with:
;FAIL in (check-accounts) (at test.clj:4:4)
;expected: {"Savings" 100, "Investments" 1000}
;  actual:
;Account Savings should have 100, but have 200
;Account Investments isn't present on list of accounts
```

## Mocks and Stubs
Currently, there's a `mocking` macro that allows you to mock some requests. Currently, only stubs are supported - something that will stub your global vars and return a value. Support for mocks, spies, etc is planned.

```clojure
(require '[check.core :refer [check]]
         '[check.mocks :refer [mocking]])

;; You define your mocks with `mocking` macro:

(deftest some-test
  (mocking
    (http/get "http://localhost:8000") => {:body "Hello, world!"}
    ; Simulating different requests every code
    (http/post "http://localhost:8000" {:foo "BAR"}) =streams=> [:ok :fail]
    ---
    (check (http/get "http://localhost:8000") => {:body string?})
    (check (http/post "http://localhost:8000") => :ok)
    (check (http/post "http://localhost:8000") => :fail)
```

## License

Copyright © 2018 Maurício Szabo

Distributed under the Eclipse Public License either version 1.0 or any later version.
