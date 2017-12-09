#!/usr/bin/env bash

#
# Post-release script that generates API docs and puts them in the Web Site's source tree.
#

if  [[ -z ${TRAVIS_TAG} ]] || [ "$TRAVIS_PULL_REQUEST" == 'true' ]; then
  echo "Skipping uploading of api docs to api.cucumber.io"
  exit 0
fi

# Clone the repo
git clone --depth=1 https://github.com/cucumber/api.cucumber.io.git
mkdir -p api.cucumber.io/cucumber-jvm/${TRAVIS_TAG}

# Create the javadoc
mvn --quite javadoc:aggregate
cp -R target/site/apidocs/ api.cucumber.io/cucumber-jvm/${TRAVIS_TAG}/javadoc

# Setup up credentials
cd api.cucumber.io
git config credential.helper "store --file=.git/credentials"
echo "https://${GH_TOKEN}:@github.com" > .git/credentials

# Commit and push
git add cucumber-jvm/${TRAVIS_TAG}/javadoc
git commit -m "Add javadoc for cucumber-jvm ${TRAVIS_TAG}"
git push

