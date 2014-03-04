#!/bin/sh
#
# Javac 1.7 fails to compile the generated i18n classes when launched from Maven
# on OS X. So we'll launch it from a shell script instead.
# After compilation we delete the source so that Maven doesn't try to compile it.
#

set -e
mkdir -p target/classes
javac -classpath src/main/java -d target/classes `find target/generated-sources -name *.java`
rm -rf target/generated-sources/i18n/java