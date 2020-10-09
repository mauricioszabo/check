# Change Log

## MISSING
- Documentation

## 0.2.0
- Kept `check.async` as `check.async-old` to ease updates
- Changed async-test to use Promises on CLJS and go-blocks on CLJ
- Added a check.async/check that will await promises or take go-blocks
- Removing `=resolves=>` arrow

## 0.1.1
- First cut of mocks library

## 0.1.0
- Wrap matcher-combinators checkers
- Support for custom matchers
- Make async tests work better with, for example, devcards

## 0.0.3
- Lots of things broke when we used `check*`, so it's removed again
- `def-async-test` works again with `check`
- Added `=resolves=>` arrow
- Ported to Shadow-CLJS

## 0.0.2-SNAPSHOT
- Bugfix -> `check` now works with Clojure and ClojureScript
- Added `check*` to use functions instead of macros

## 0.0.1-SNAPSHOT
### Added
- `check` function
- `=>` arrow function
- `=in=>` arrow function
- `def-async-test` function
- Some simple fixes to work with Clojure and ClojureScript
- `=throws=>` arrow
