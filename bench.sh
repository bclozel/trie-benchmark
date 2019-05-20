#!/bin/sh
mvn clean package
java -jar target/benchmarks.jar -f 1 -prof gc
