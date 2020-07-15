#!/bin/bash
echo "installing a snapshot version"
lein change version str "-SNAPSHOT"
lein install

lein change version clojure.string/replace '#"-SNAPSHOT"' '""'
