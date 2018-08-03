#!/bin/bash
export USERID=$(id -u)
export PATH="$PATH:/usr/local/bin"
export GROUPID=$(id -g)
cd $(dirname $0)
docker-compose -f test-bed.yml run --rm -w "$WORKSPACE" --entrypoint "SBI/runtests.sh" maven-app-build
