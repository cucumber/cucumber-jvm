Cucumber DeltaSpike
===================

This module relies on [DeltaSpike Container Control](https://deltaspike.apache.org/documentation/container-control.html) to start/stop supported CDI container.

## Setup
Enable cdi support for your steps by adding an (empty) beans.xml into your classpath (src/main/resource/META-INF for normal classes or src/test/resources/META-INF for test classes):

```xml
<beans xmlns="http://java.sun.com/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
      http://java.sun.com/xml/ns/javaee
      http://java.sun.com/xml/ns/javaee/beans_1_0.xsd">

</beans>
```

To use dependency injection, add `@Inject` to any field which should be managed by CDI. For more information, see [JSR330](https://www.jcp.org/en/jsr/detail?id=330).

```java
public class BellyStepdefs {

    @Inject
    private Belly belly;

    //normal step code ...
}
```

This object factory doesn't start or stop any [Scopes](https://docs.oracle.com/javaee/6/tutorial/doc/gjbbk.html), so all beans live inside the default scope (Dependent). Now Cucumber requested an instance of your step definitions for every step, which means cdi create a new instance for every step and for all injected fields. This behaviour makes it impossible to share a state inside a scenario.

To bypass this, you must annotate your class(es) with `@javax.inject.Singleton`:
1. on destinations: now the object factory will create only one instance include injected fields per scenario, and both injected fields and step definitions can be used to share state inside a scenario.
2. on any other class: now the object factory will create a new instance of your step definitions per step and step definitions can not be used to share state inside a scenario, only the annotated classes can be used to share state inside a scenario

You can also combine both approaches.

```java
@Singleton
public class BellyStepdefs {

    @Inject
    private Belly belly;

    //normal step code ...
}
```
It is not possible to use any other scope than Dependent. This means also it is not possible to share a state over two or more scenarios; every scenario starts with a clean environment.

To enable this object factory, add the following dependency to your `pom.xml`
and use the [`cucumber-bom`](../cucumber-bom/README.md) for dependency management:

```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-deltaspike</artifactId>
    <version>${cucumber.version}</version>
    <scope>test</scope>
</dependency>
```

and one of the supported cdi-containers.

To use it with Weld:

```xml
<dependency>
    <groupId>org.apache.deltaspike.cdictrl</groupId>
    <artifactId>deltaspike-cdictrl-weld</artifactId>
    <version>${deltaspike.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.jboss.weld.se</groupId>
    <artifactId>weld-se-core</artifactId>
    <version>${weld-se.version}</version>
    <scope>test</scope>
</dependency>
```

To use it with OpenEJB:

```xml
<dependency>
    <groupId>org.apache.deltaspike.cdictrl</groupId>
    <artifactId>deltaspike-cdictrl-openejb</artifactId>
    <version>${deltaspike.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.apache.tomee</groupId>
    <artifactId>openejb-core</artifactId>
    <version>${openejb-core.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>${jaxb-api.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>${jaxb-api.version}</version>
    <scope>test</scope>
</dependency>
```

To use it with OpenWebBeans:

```xml
<dependency>
    <groupId>org.apache.deltaspike.cdictrl</groupId>
    <artifactId>deltaspike-cdictrl-owb</artifactId>
    <version>${deltaspike.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.apache.openwebbeans</groupId>
    <artifactId>openwebbeans-impl</artifactId>
    <version>${owb.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>javax.annotation</groupId>
    <artifactId>jsr250-api</artifactId>
    <version>1.0</version>
    <scope>test</scope>
</dependency>
```

Some containers need you to provide a CDI-API in a given version, but if you develop CDI and use one of the above containers, it should already be on your path.
