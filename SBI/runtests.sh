#!/bin/bash
mvn test
RESULT=$?

[ ! -d jenkins-test-results ] && mkdir jenkins-test-results

clover=$(find . -name clover.xml)
if [ -n "$clover" ]
then
    cp -p $clover jenkins-test-results/
fi
