# Using Cucumber Metrics

Cucumber Metrics report allows for the use of your methods ( java) Cucumber.

## Configuration via Cucumber Guice

Cucumber Metrics use Cucumber Guice for configuration.

[Read package documentation online at cukes.info]
(http://cukes.info/api/cucumber/jvm/javadoc/cucumber/api/guice/package-summary.html) 

[Read package documentation offline (raw html)](src/main/java/cucumber/api/guice/package.html)

Add guice.injector-source=cucumber.metric.regulator.injector.CucumberMetricsInjectorSource in src\test\resources\cucumber.properties (in your project)

## how to use Cucumber Metrics

Cucumber Metrics use java annotations:

### @Timed

@Timed
@Given("^me a hello, please. Best Regards '(.*)'.$")
public void hello(String name) throws InterruptedException, InstantiationException, IllegalAccessException {
    logger.info("Hello " + name + "!");
}

Every time your scenarios using the method "hello" you get the following trace:

Cucumber Metrics TimedInterceptor invoke method public void your.package.your.class.hello(java.lang.String) throws java.lang.InterruptedException,java.lang.InstantiationException,java.lang.IllegalAccessException is called on your.package.your.class$$EnhancerByGuice$$9aa0e4ca@cda4919 with args [Ljava.lang.Object;@7f4d9395

### @SpeedRegulator

In progress...