This is a TestNG copy-paste version of the [JUnit Calculator example](https://github.com/cucumber/cucumber-jvm/tree/main/examples/java-calculator) project.

If you find its TestNG report is not idiomatic, consider making a contribution to improve Cucumber JVM TestNG Support.  

Two runner classes examplify the available alternatives:
* Let the [runner](src/test/java/io/cucumber/examples/testng/RunCucumberTest.java) inherit AbstractTestNGCucumberTests. Each scenario will then be executed as a separate TestNG test.
* If the runner need to have another base class:
  * let the [runner](src/test/java/io/cucumber/examples/testng/RunCucumberByCompositionTest.java) use the same structure as AbstractTestNGCucumberTests to make each scenario run as a separate TestNG test.

**Note**
Please keep code base of this project up-to-date

