#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
  mvn deploy -P-examples -Psign-source-javadoc-travis --settings continuous-deployment/travis-settings.xml -DskipTests=true
else
  echo "Artifacts are only deployed on a build of the master branch"
fi
