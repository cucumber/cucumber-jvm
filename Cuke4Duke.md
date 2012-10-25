## Migration from Cuke4Duke

### POM

For those of you that have run Cuke4Duke via Maven in the past, and perhaps have a bulk of feature files to migrate,
here is a quick guide to getting set up.

The first step is getting the POM.xml in your project configured with the right artifacts. 

Example Cucumber-JVM + Maven + Groovy CLI
( http://pastebin.com/XEmhuxkK )

This configuration will get you the following features:

* Groovy-only: No JUnit/Java code will be run
* Tags: Multiple tags cannot be passed in from a JVM argument at this time, but multiple items can be added to the POM. Note that ~@ignore is in here by default, as an example. In addition, you can provide -DtagArg="@tagname" to run any tag
* Formats: the <format> property can be changed from 'pretty' to 'html', or 'progress'. 
 * If html format is used, the --out parameter must be provided and set to a folder (relative to target) to dump the reports
 * The previous example, with target/reports specified as the output dir: ( http://pastebin.com/GrWN3ULN )

### Project structure

Next you'll want to structure your feature and step definition files according to the Cucumber-JVM hierarchy (quite a bit different than Cuke4Duke in most cases)

```
src/
  test/
    resources/
      featurefile.feature
      com/
        yourcompany/
          stepdefinitions.groovy
```

### Step definition changes

The only initial difference that will need to be made is to switch the metaclass mixin:

```
this.metaClass.mixin(cuke4duke.GroovyDsl)
```

over to the Cucumber-JVM way...

```
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN) // Or any other supported language
```

Past that, there may be slight differences in the groovy coding aspects, but nothing too earth shattering
//TODO: Quantify what it takes to shatter an earth

### Run it!

To run all features:

``` 
mvn clean test

```

To specify a tag:

``` 
mvn clean test -Dcucumber.options="--tags @mytag"

```
