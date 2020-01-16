Release Notes
=============

## Cucumber-JVM v5.0.0

Last year [we announced v5.0.0-RC1]. Today we're announcing v5.0.0! This release
was made possible by significant contributions and test driving from - in no
particular order;  Anton Deriabin, Alexandre Monterroso, Marit Van Dijk and
David Goss, Dominic Adatia, Marc Hauptmann, John Patrick and Loïc Péron.

We'll cover some of notable changes since the announcement of v5.0.0-RC1. As
always the [full changelog] can be found in the repository. 

[we announced v5.0.0-RC1]:  https://cucumber.io/blog/announcing-cucumber-jvm-v5-0-0-rc1/
[full changelog]:           CHANGELOG.md

### Empty cells in data tables

In RC1 we already introduced a change that would convert empty cells to `null`
values rather then the empty string. However there are use cases where an empty
string is actually desired. 

By declaring a table transformer with a replacement string it becomes‌
‌possible to explicitly disambiguate between the two cases. For‌
‌example:
‌
```gherkin
Given some authors
   | name            | first publication |
   | Aspiring Author |                   |
   | Ancient Author  | [blank]           |
```

```java
package com.example.app;

import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;

import java.util.List;

public class StepDefinitions {
    @DataTableType(replaceWithEmptyString = "[blank]")
    public Author convert(Map<String, String> entry){
      return new Author(
         entry.get("name"),
         entry.get("first publication")
      );
    }
    
    @Given("some authors")
    public void given_some_authors(List<Author> authors){
      // authors = [Author(name="Aspiring Author", firstPublication=null), Author(name="Ancient Author", firstPublication=)]
    }
}
```

### Sharing the Spring Application Context

The `cucumber-spring` module provides dependency injection using the Spring
application context. The application context can take a long time to start up so
Springs `TestContextManager` framework will share identical application contexts
between tests. 

However to use step definitions Cucumber has to modify the application context.
When executing in parallel step definitions were registered concurrently and
this resulted in several race conditions. So as a workaround Cucumber would
create a new application context for each thread.

Some sleuthing by Dominic Adatia uncovered the root cause of the race
conditions and a solution to solve it properly. As an additional benefit
Cucumber will also share the application context with other unit tests
improving performance somewhat more.

### Deprecating `--non-strict`

In most frameworks tests can either be skipped, failed, or succeed. In Cucumber,
they can also be pending or undefined. Tests that are skipped or succeeded do
not fail the build. But depending on your opinion of work-on-in-progress,
pending and undefined test might.

Cucumber facilitates provides the `--strict` and `--non-strict` execution options
which makes work in progress fail or pass respectively. This flexibility comes
at a cost. Tools that interpet Cucumbers output also have to be configured with
either the --strict or the --non-strict option and these configurations have to
be consistent.

While `--non-strict` has been the default for a long time we are now of the
opinion that work in progress is a failing state. This means that in proper TDD
fashion when given a feature file, it will not pass until all steps have been
implemented and made to pass (note: Cucumbers generated step definitions throw
a pending exception). To make this transition graceful Cucumber will log a
warning when using --non-strict. The warning can be suppressed by using
`--strict`. Eventually we'll remove the `--non-strict` option and make 
`--strict` the default behaviour.

### JUnit 5 Support

Cucumber-JVM now has JUnit5 support. To use it add the
`cucumber-junit-platform-engine` dependency to your project. 
‌
```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit-platform-engine</artifactId>
    <version>${cucumber.version}</version>
    <scope>test</scope>
</dependency>
```

You can provide options by adding a `junit-platform.properties` file to your
classpath root. Below are the supported options.
‌
```properties
cucumber.ansi-colors.disabled=                          # true or false. default: true
cucumber.execution.dry-run=                             # true or false. default: false
cucumber.glue=                                          # comma separated package names. example: com.example.glue
cucumber.plugin=                                        # comma separated plugin strings. example: pretty, json:path/to/report.json
cucumber.object-factory=                                # object factory class name. example: com.example.MyObjectFactory
cucumber.snippet-type=                                  # underscore or camelcase. default: underscore
cucumber.execution.parallel.enabled=                    # true or false. default: false
cucumber.execution.parallel.config.strategy=            # dynamic, fixed or custom. default: dynamic
cucumber.execution.parallel.config.fixed.parallelism=   # positive integer. example: 4
cucumber.execution.parallel.config.dynamic.factor=      # positive double. default: 1.0
cucumber.execution.parallel.config.custom.class=        # class name. example: com.example.MyCustomParallelStrategy
```

Altogether your project should look like this.
‌
```
├─ pom.xml or build.gradle
├─ src/main/java/
|  └─ com/example/app/
|     └─ Application.java
├─ src/test/java/
|  └─ com/example/app/
|     └─ ApplicationStepDefinitions.java
└─ src/test/resources/
   ├─ junit-platform.properties
   └─ com/example/app/
      └─ application.feature
```
‌
For more details see the [junit-platform-engine/README.md].

[junit-platform-engine/README.md]: junit-platform-engine/README.md

#### Tooling Support

So Cucumber has JUnit5 support. What does this actually mean and how do I run my
features? JUnit5 consists of three parts. The JUnit Platform, JUnit Jupiter, 
and JUnit Vintage. The JUnit Platform is a framework to develop test engines.
Examples of these would be JUnit Jupiter and JUnit Vintage. 

Cucumber implements the JUnit Platform API. Any one using Cucumber and the JUnit
Platform will be able to discover, filter and execute feature files as if they
were just another test. As Cucumber now implements a popular API it should be
easier to integrate with build systems and IDEs.

[JUnit5 consists of three parts]:   https://junit.org/junit5/docs/current/user-guide/#overview-what-is-junit-5


#### Running with the Console Launcher

The JUnit 5 project provides a `ConsoleLauncher`. You can use this to run
Cucumber. See the [JUnit5 documentation] for details. This will ouput something
like this.
‌
```
╷
└─ Cucumber ✔
   ├─ A feature with scenario outlines ✔
   │  ├─ A scenario ✔
   │  ├─ A scenario outline ✔
   │  │  ├─ With some text ✔
   │  │  │  ├─ Example #1 ✔
   │  │  │  └─ Example #2 ✔
   │  │  └─ With some other text ✔
   │  │     ├─ Example #1 ✔
   │  │     └─ Example #2 ✔
   │  └─ A scenario outline with one example ✔
   │     └─ Examples ✔
   │        ├─ Example #1 ✔
   │        └─ Example #2 ✔
   └─ A feature with a single scenario ✔
      └─ A single scenario ✔

Test run finished after 2588 ms
[         8 containers found      ]
[         0 containers skipped    ]
[         8 containers started    ]
[         0 containers aborted    ]
[         8 containers successful ]
[         0 containers failed     ]
[         8 tests found           ]
[         0 tests skipped         ]
[         8 tests started         ]
[         0 tests aborted         ]
[         8 tests successful      ]
[         0 tests failed          ]
```
[JUnit5 documentation]: ‌https://junit.org/junit5/docs/current/user-guide/#running-tests-console-launcher

#### Maven Surefire and Gradle

While Maven Surefire and Gradle support the JUnit Platform they do not yet use
it for test discovery<sup>[1],[2],[3]</sup>.  So a work around is needed. Add
this marker class to the package containing your feature files. Then run your
build system as you would normally
‌
```java
package com.example.app;

import io.cucumber.junit.platform.engine.Cucumber;

@Cucumber
public class RunCucumberTest {
}
```

‌Now your project should look like this:
‌
```
├─ pom.xml or build.gradle
├─ src/main/java/
|  └─ com/example/app/
|     └─ Application.java
├─ src/test/java/
|  └─ com/example/app/
|     ├─ ApplicationStepDefinitions.java
|     └─ RunCucumberTest.java
└─ src/test/resources/
   ├─ junit-platform.properties
   └─ com/example/app/
      └─ application.feature
```

[1]:    https://issues.apache.org/jira/browse/SUREFIRE-1724
[2]:    https://issues.apache.org/jira/browse/SUREFIRE-1337
[3]:    https://github.com/gradle/gradle/issues/4773

#### IDEA and Eclipse

IDEA and Eclipse have some support for the JUnit Platform<sup>[4]</sup>. Currently you 
can select package and run all features in it. It is not yet possible to select
single files or scenarios. This may become easier once support for file based
test engines improves<sup>[5]</sup>. 

[4]:    https://youtrack.jetbrains.com/issue/IDEA-227508
[5]:    https://github.com/junit-team/junit5/issues/2146