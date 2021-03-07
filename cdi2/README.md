Cucumber CDI 2
==============

This module relies on CDI Standalone Edition (CDI SE) API to start/stop a CDI container
and customize it - adding steps. It looks up the beans/steps in CDI and if not available
it instantiates it as POJO with CDI injection support - unmanaged bean.

## Setup

To use it, it is important to provide your CDI SE implementation - likely Weld or Apache OpenWebBeans.

For Apache OpenWebBeans the dependency is:

```xml
<dependency>
  <groupId>org.apache.openwebbeans</groupId>
  <artifactId>openwebbeans-se</artifactId>
  <version>2.0.10</version>
  <scope>test</scope>
</dependency>

```

And for Weld it is:

```xml
<dependency>
  <groupId>org.jboss.weld.se</groupId>
  <artifactId>weld-se-core</artifactId>
  <version>3.1.1.Final</version>
  <scope>test</scope>
</dependency>
```

To ensure the module is compatible with all implementations and future API version, it does not transitively bring the API.
If you don't know which one to use, you can import the following one but if you develop CDI code you should already have one provided:

```xml
<dependency>
  <groupId>javax.enterprise</groupId>
  <artifactId>cdi-api</artifactId>
  <version>2.0</version>
</dependency>
```

## Configuration

System properties are used to customize the CDI `SeContainerInitializer`, here is the list:

* `cucumber.cdi2.packages`: list of packages (comma separated) added in the container discovery, must contain a `package-info.class` file to be deterministic.
* `cucumber.cdi2.recursivePackages`: same as previous one but recursively.
* `cucumber.cdi2.classes`: list of classes added in the discovery.
* `cucumber.cdi2.extensions`: list of extension classes added to the container (if not automatically registered).
* `cucumber.cdi2.<key>`: will add the system property as a key/value property of the initializer (container specific).

Note that you can also implement `io.cucumber.cdi2.spi.SeContainerInitializerCustomizer` and register your implementation by putting its fully qualified name in `META-INF/services/io.cucumber.cdi2.spi.SeContainerInitializerCustomizer` to customize the initializer.
