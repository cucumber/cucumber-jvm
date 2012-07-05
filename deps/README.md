# Cucumber-Deps

This module packages XStream and DiffUtils into a new Jar, under new packages.
This is to prevent conflicts.

The project is not part of the top level build (pom.xml) and must be run separately.
The jar file is weaved into the cucumber-core jar file when it builds.
