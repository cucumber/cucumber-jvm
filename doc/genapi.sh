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
mvn javadoc:aggregate
cp -R target/site/apidocs/ ../api.cucumber.io/cucumber-jvm/${1}/javadoc