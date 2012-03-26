This project documents how Cucumber-JVM's JUnit integration breaks when parallel tests are enabled via Maven's Surefire plugin.
Issue: https://github.com/cucumber/cucumber-jvm/issues/86

==========================================================================================


1. Sequential run: 'mvn clean install'
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.chrisgleissner.cucumberjvm.PersonalGrowth1Test
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.298 sec
Running com.chrisgleissner.cucumberjvm.PersonalGrowth2Test
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 sec

Results :

Tests run: 12, Failures: 0, Errors: 0, Skipped: 0



==========================================================================================


2. Parallel test: 'mvn clean install -Pparallel'
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Concurrency config is parallel='classes', perCoreThreadCount=true, threadCount=2, useUnlimitedThreads=false
You can implement missing steps with the snippets below:
@Given("^a person of (\\d+) feet$")
public void a_person_of_feet(int arg1) {
    // Express the Regexp above with the code you wish you had
}
@Then("^the person will be (\\d+) feet tall$")
public void the_person_will_be_feet_tall(int arg1) {
    // Express the Regexp above with the code you wish you had
}
@When("^the person grows by (\\d+) feet$")
public void the_person_grows_by_feet(int arg1) {
    // Express the Regexp above with the code you wish you had
}

Results :

Tests run: 0, Failures: 0, Errors: 0, Skipped: 0


==========================================================================================

Environment set-up: 'mvn -v'
Apache Maven 3.0.3 (r1075438; 2011-02-28 10:31:09-0700)
Maven home: /usr/share/maven
Java version: 1.6.0_26, vendor: Apple Inc.
Java home: /System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home
Default locale: en_US, platform encoding: MacRoman
OS name: "mac os x", version: "10.7.2", arch: "x86_64", family: "mac"

