## Running from the Command Line Interface (CLI)

    groovy bin/cucumber.groovy --glue src/test/resources src/test/resources/cucumber/runtime/groovy/a_feature.feature

This demonstrates that the files in the bin directory (`cucumber-jvm.groovy` and `cucumber-groovy-full.jar` are a completely standalone
execution environment. TODO: Figure out the best way to package and publish this as a "groovy package". Maybe just a zip file?
