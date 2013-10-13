If you intend to build this project with Ant or with an IDE,
you will need to have the required jars inside this directory.

If you're building this project with **Ant,** just run ../build.xml
and the required jars will **automatically be downloaded.**

### Required jars for Cukeulator Test App
* cucumber-core
* cucumber-java
* cucumber-android
* cucumber-jvm-deps-1.0.3 (shouldn't be necessary)
* cucumber-picocontainer (shouldn't be necessary)
* picocontainer-2.14.3 (shouldn't be necessary)
* gherkin-2.12.1 (shouldn't be necessary)
* cucumber-html (only required for HTML reports)


* To download the release versions run `ant -f init.xml`.
* Or run `./init.sh` to build snaphsots with Maven.

*Note for Eclipse users: The IDE should automatically include all .jars from the libs/ directory.*

*Note for IDEA users: You need to manually include jars from /libs for your module.*
