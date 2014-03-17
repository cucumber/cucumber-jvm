#!/bin/sh
#
# Javac 1.7 fails to compile the generated i18n classes when launched from Maven
# on OS X. So we'll launch it from a shell script instead.
# After compilation we delete the source so that Maven doesn't try to compile it.
#
# This script is only run when building on OS X. On other operating systems Maven
# will not run this script and compilation happens in the usual way.

set -e
mkdir -p target/classes
javac -encoding UTF-8 -classpath src/main/java -d target/classes `find target/generated-sources -name *.java`

# Back up the bare minimum (English)
mv target/generated-sources/i18n/java/cucumber/api/java/en target/java-en
# Delete everything else
rm -rf target/generated-sources/i18n/java/cucumber/api/java/*
# Restore the backup of English sources so that compilation still works in the IDE.
mv target/java-en target/generated-sources/i18n/java/cucumber/api/java/en
