#!/bin/bash
mvn test
RESULT=$?

#[ ! -d jenkins-cucumber-results ] && mkdir jenkins-cucumber-results

#cucumber=$(find . -name cucumber-report*.json -type f)
#if [ -n "$cucumber" ]
#then
   # cp -p $cucumber jenkins-cucumber-results/
#fi
exit $RESULT
