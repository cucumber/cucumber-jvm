#!/bin/bash

# Build required jars
mvn package -pl android -am -P android -f ../../../../pom.xml

# Copy jars to this location
cp ../../../../core/target/cucumber-core-*.jar ./
cp ../../../../java/target/cucumber-java-*.jar ./
cp ../../../../android/target/cucumber-android-*.jar ./
