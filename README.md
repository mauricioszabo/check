# Check

Library helpers for a better testing world.

## Motivation
Clojure's default test library is... well... limited, to say the least. There are better
options out there, but `expectations` is too opinated in "one assertion per test", `midje`
is **way too magic** (and also don't work on ClojureScript), `speclj` is probably dead
(and don't work with async tests).

So, enters this library: it wraps `expectations` so we're not trying to reinvent the
wheel, uses `core.async` to transform simulate synchronous code on ClojureScript async
tests, and uses "midje-style" arrows. Everybody wins!

## Usage

You can use the "arrow" expectations the same way you would use `expectations` lib:

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

You can use `def-async-test` to generate a test that will timeout after a while
```clojure
(require '[check.async :refer [def-async-test await!]]
         '[clojure.core.async :refer [chan >! timeout go <!]])

(def-async-test "checks for async code" {}
  (let [c (chan)]
    (future
      (Thread/sleep 400)
      (>! c :done))
    (check (await! c) => :done)))
```

## Extending

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
