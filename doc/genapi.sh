#!/bin/bash
#
# Post-release script that generates API docs and puts them in the Web Site's source tree.
#

if [ "$#" -ne 1 ]; then
  echo "Requires the version as the only argument"
  echo "Usage: $0 VERSION"
  exit 1
fi

mkdir -p ../api.cucumber.io/cucumber-jvm/${1}

# JavaDoc
pushd target/checkout/
  mvn javadoc:aggregate
  cp -R target/site/apidocs/ ../../../api.cucumber.io/cucumber-jvm/${1}/javadoc/
popd

# ScalaDoc
pushd target/checkout/scala/
  mvn scala:doc
  cp -R scala_2.12/target/site/scaladocs/ ../../../../api.cucumber.io/cucumber-jvm/${1}/scaladoc/
popd

# Yardoc (Ruby)
pushd target/checkout/jruby/
  rake yard
  cp -R doc/ ../../../../api.cucumber.io/cucumber-jvm/${1}/yardoc/
popd
