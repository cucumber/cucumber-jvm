#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

rm -rf ${DIR}/src/test/resources/*
cp -r ${DIR}/../../cucumber/compatibility-kit/javascript/features ${DIR}/src/test/resources/
