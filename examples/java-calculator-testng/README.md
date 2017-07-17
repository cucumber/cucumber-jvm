This is a TestNG copy-paste version of the [JUnit Calculator example](https://github.com/cucumber/cucumber-jvm/tree/master/examples/java-calculator) project.

If you find its TestNG report is not idiomatic, consider making a contribution to improve Cucumber JVM TestNG Support.  

Two runner classes examplify the available alternatives:
* Let the [runner](src/test/java/cucumber/examples/java/calculator/RunCukesTest.java) inherit AbstractTestNGCucumberTests. Each scenario will then be executed as a separate TestNG test.
* If the runner need to have another base class:
  * let the [runner](src/test/java/cucumber/examples/java/calculator/RunCukesByCompositionTest.java) use the same structure as AbstractTestNGCucumberTests to make each scenario run as a separate TestNG test.

**Note**
Please keep code base of this project up-to-date

