# Running Cucumber-JVM with step definitions in Java using Gradle's `javaexec` task

## Credits

This work is based on [dkowis/cucumber-jvm-groovy-example](https://github.com/dkowis/cucumber-jvm-groovy-example)

## Motivation

There exists [a number of issues](http://gradle.1045684.n5.nabble.com/Gradle-and-cucumber-jvm-tt5710562.html) which prevent seamless integration of Cucumber-JVM and Gradle.

## Solution

One possible solution is to use Cucumber's `Main` class to run your tests. You can do this by using the `javaexec` task in Gradle.

## Running

In order to run your Cucumber tests execute:

```sh
gradle cucumber
```

## Caveats

The Groovy example by [David Kowis](https://github.com/dkowis) runs perfectly, but it uses Groovy step definitions.

If you're writing your step definitions in Java then the Gradle script needs to be changed slightly.

Here are some caveats:

 * The `cucumber` task has to depend on `compileTestJava` task in order to compile test sources

 ```groovy
 task cucumber() {
     dependsOn assemble, compileTestJava
     ...
 }
 ```

 * The `javaexec` classpath should include `main` and `test` output directories.
 Otherwise Cucumber-JVM will not find your production classes/resources and step definitions respectively.

 ```groovy
 classpath = configurations.cucumberRuntime + sourceSets.main.output + sourceSets.test.output
 ```

 * Cucumber's `--glue` should be set to your package name (e.g. `gradle.cucumber`) and **NOT** to `src/test/java`

 ```groovy
 args = ['-f', 'pretty', '--glue', 'gradle.cucumber', 'src/test/resources']
 ```



