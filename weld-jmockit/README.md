# cucumber-weld-jmockit
cucumber-weld-jmockit provides integration of [JMockit](https://github.com/jmockit/jmockit1) with [Cucumber-Weld](https://github.com/cucumber/cucumber-jvm/tree/master/weld).

To integrate JMockit with cucumber-weld you have two options: either use a CDI extension or an interceptor.

## CDI extension
To use the CDI extension you can register the extension as a service provider by creating a file named _META-INF/services/javax.enterprise.inject.spi.Extension_, which contains the name of the extension class:

```
cucumber.runtime.java.weld.jmockit.WeldJMockitExtension
```

The extension will register an interceptor _(cucumber.runtime.java.weld.jmockit.JMockitInterceptor)_ to all step definitions (glue code) which provides the integration between JMockit, Cucumber and Weld.

## Interceptor
Alternatively, e.g. in case you do not want to use the CDI extension, you may want to use the interceptor directly. Therefore, simply annotate step definitions (glue code) with _@cucumber.runtime.java.weld.jmockit.WithJMockit_ annotation like so:

```java
import cucumber.api.java.en.Given;
import cucumber.runtime.java.weld.jmockit.WithJMockit;

@WithJMockit
public class Stepdefs {

    @Given("^I have (\\d+) cukes in my belly$")
    public void I_have_cukes_in_my_belly(int cukes) throws Throwable {
    }

}
```
