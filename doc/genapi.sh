#!/bin/bash
#
# Post-release script that generates API docs and puts them in the Web Site's source tree.
#

# JavaDoc
cd target/checkout/
mvn javadoc:aggregate
rm -Rf ../../../cucumber.github.com/api/cucumber/jvm/javadoc/
cp -R target/site/apidocs/ ../../../cucumber.github.com/api/cucumber/jvm/javadoc/
cd -

# ScalaDoc
cd target/checkout/scala/
mvn scala:doc
cp -R target/site/scaladocs/ ../../../../cucumber.github.com/api/cucumber/jvm/scaladoc/
cd -

# Yardoc (Ruby)
cd target/checkout/jruby/
rake yard
cp -R doc/ ../../../../cucumber.github.com/api/cucumber/jvm/yardoc/
cd -