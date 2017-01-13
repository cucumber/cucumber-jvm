# Using Cucumber Metrics

Cucumber Metrics report allows for the use of your methods ( java) Cucumber.

## Configuration via Cucumber Guice

Cucumber Metrics use Cucumber Guice for configuration.

[Read package documentation online at cukes.info]
(http://cukes.info/api/cucumber/jvm/javadoc/cucumber/api/guice/package-summary.html) 

[Read package documentation offline (raw html)](src/main/java/cucumber/api/guice/package.html)

Add guice.injector-source=cucumber.metrics.regulator.injector.CucumberMetricsInjectorSource in src\test\resources\cucumber.properties (in your project)

## how to use Cucumber Metrics

Cucumber Metrics use java annotations:

### @Time and @Times

@Times is a list of @Time

name: name or key of metric (name of java method by default)

mark: Weight of increment (1 by default)

    @Time
    @Given("^me a hello, please. Best Regards '(.*)'.$")
    public void hello(String name) {
        logger.info("Hello " + name + "!");
    }

or

    @Time(name = "{nameKey}")
    @Given("^me a hello, please. Best Regards '(.*)'.$")
    public void hello(@TimeName("nameKey") String name, @TimeValue("nb") int nb) {
        for (int i = 0; i < nb; i++) {
            logger.info("Hello " + name + "!");
        }
    }
    
or

    @Times({ @Time(name = "OTHER_KEY"), @Time(name = "{nameKey}") })
    @Given("^me a hello, please. Best Regards '(.*)'.$")
    public void hello(@TimeName("nameKey") String name, @TimeValue("nb") int nb) {
        for (int i = 0; i < nb; i++) {
            logger.info("Hello " + name + "!");
        }
    }    

Every time your scenarios using the method "hello" you get the following trace:

Cucumber Metrics TimedInterceptor invoke method public void your.package.your.class.hello(java.lang.String) is called on your.package.your.class$$EnhancerByGuice$$9aa0e4ca@cda4919 with args [Ljava.lang.Object;@7f4d9395

### @SpeedRegulators and @SpeedRegulator

@SpeedRegulators is a list of @SpeedRegulator

    @SpeedRegulators({ @SpeedRegulator(application = "APP_1", cost = 500000000), @SpeedRegulator(application = "APP_2", cost = 2000000000) })
    @Given("^me a hello, please. Best Regards '(.*)'.$")
    public void hello(String name) {
        logger.info("Hello " + name + "!");
    }

application: name or key of targeted application

cost: downtime (in nano second) for the targeted application (example: 0.5s = 500000000)

## JMX

All metrics of @Time and @Times are available in the JVM via JMX. 

On windows, you can use jconsole of Java (C:\Program Files\Java\jdk1.8.0_66\bin\jconsole.exe)

![jconsole](/metrics/screenshots/jconsole.png)

![jconsole2](/metrics/screenshots/jconsole2.jpg)
