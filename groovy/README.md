## Running from the Command Line Interface (CLI)

To run the test from the cli call:

    groovy -cp target/test-classes ./bin/cucumber-jvm.groovy --glue src/test/resources --glue cucumber/runtime/groovy src/test/resources/cucumber/runtime/groovy/a_feature.feature

or

    groovy -cp target/test-classes ./bin/cucumber-jvm.groovy --glue src/test/resources --glue cucumber.runtime.groovy src/test/resources/cucumber/runtime/groovy/a_feature.


The test uses a mix of compiled and interpreted step definitions which makes the command a bit tricky:

1. `-cp target/test-classes` tells groovy where to find the compiled class files
2. `--glue src/test/resources` is required so that cucumber finds the interpreted step definitions
3. `--glue cucumber/runtime/groovy` or `--glue cucumber.runtime.groovy` is required so that cucumber finds the compiled step definitions
4. the last parameter provides a feature or a path with features

This demonstrates that the files in the `bin` directory (`cucumber-jvm.groovy`, `cucumber-groovy.jar` and `cucumber-core.jar` 
are a completely standalone execution environment. 

TODO: Figure out the best way to package and publish this as a "groovy package". Maybe just a zip file?
