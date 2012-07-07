# Cucumber-Deps

This module packages XStream and DiffUtils into a new Jar, under new packages.
This is to prevent conflicts. After `mvn install` the jars get copied to `repository`
(which is added to git). Other modules will pick it up.

The project is not part of the top level build (pom.xml) and must be run separately.
The jar file is weaved into the cucumber-core jar file when it builds, so the cucumber-deps
jar doesn't have to be available in any online repos.
