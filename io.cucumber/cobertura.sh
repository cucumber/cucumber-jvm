#!/bin/sh

if [ -z "$COBERTURA_HOME" ]; then
    echo "You need to define COBERTURA_HOME"
    exit 1
fi

mvn clean compile
ant -f cobertura.xml instrument
mvn test
ant -f cobertura.xml report
