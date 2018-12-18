# Change Log

## FUTURE
- Fix error when an exception does not checks with `=throws=>`
- Documentation
- Make CI understand failures (currently we're mocking Clojure's test reporter, but still marks a lot of things as failures)

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
