Cucumber DeltaSpike
===================

This module relies on [DeltaSpike Container Control](https://deltaspike.apache.org/documentation/container-control.html) to start/stop supported CDI container.

## Setup
Enable cdi support for your steps by adding a (empty) beans.xml into your classpath (src/main/resource/META-INF for normal classes or src/test/resources/META-INF for test classes):

```xml
<beans xmlns="http://java.sun.com/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
      http://java.sun.com/xml/ns/javaee
      http://java.sun.com/xml/ns/javaee/beans_1_0.xsd">

</beans>
```

To enable this objectfactory add the folling dependency to your classpath:
```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-deltaspike</artifactId>
    <version>${cucumber.version}</version>
    <scope>test</scope>
</dependency>
```

and one of the supported cdi-containers.

to use it with Weld:

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

or to use it with OpenEJB:

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

or to use it with OpenWebBeans:
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

Some containers need that you provide a CDI-API in a given version, but if you develop CDI and use one of the above containers it should already on your path.

