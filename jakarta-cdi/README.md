Cucumber CDI Jakarta
====================

This module relies on CDI Standalone Edition (CDI SE) API to start/stop a CDI container
and customize it - adding steps. It looks up the beans/steps in CDI and if not available
it instantiates it as POJO with CDI injection support - unmanaged bean.

IMPORTANT: it uses jakarta flavor of CDI and not javax one.

## Setup

To use it, it is important to provide your CDI SE implementation - likely Weld or Apache OpenWebBeans.

For Apache OpenWebBeans the dependency is:

```xml
<dependency>
    <groupId>org.apache.xbean</groupId>
    <artifactId>xbean-finder-shaded</artifactId>
    <version>${xbean.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.apache.xbean</groupId>
    <artifactId>xbean-asm7-shaded</artifactId> <!-- or asm8 flavor for more recent openwebbeans -->
    <version>${xbean.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.apache.openwebbeans</groupId>
    <artifactId>openwebbeans-impl</artifactId>
    <version>${openwebbeans.version}</version>
    <scope>test</scope>
    <classifier>jakarta</classifier>
    <exclusions>
        <exclusion>
            <groupId>*</groupId>
            <artifactId>*</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.apache.openwebbeans</groupId>
    <artifactId>openwebbeans-spi</artifactId>
    <version>${openwebbeans.version}</version>
    <scope>test</scope>
    <classifier>jakarta</classifier>
    <exclusions>
        <exclusion>
            <groupId>*</groupId>
            <artifactId>*</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.apache.openwebbeans</groupId>
    <artifactId>openwebbeans-se</artifactId>
    <version>${openwebbeans.version}</version>
    <classifier>jakarta</classifier>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>*</groupId>
            <artifactId>*</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

And for Weld it is:

```xml
<dependency>
  <groupId>org.jboss.weld.se</groupId>
  <artifactId>weld-se-core</artifactId>
  <version>4.0.0.Alpha2</version>
  <scope>test</scope>
</dependency>
```

To ensure the module is compatible with all implementations and future API version, it does not transitively bring the API.
If you don't know which one to use, you can import the following one but if you develop CDI code you should already have one provided:

```xml
<dependency>
    <groupId>jakarta.enterprise</groupId>
    <artifactId>jakarta.enterprise.cdi-api</artifactId>
    <version>3.0.0-M3</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>jakarta.activation</groupId>
    <artifactId>jakarta.activation-api</artifactId>
    <version>2.0.0-RC3</version>
    <scope>provided</scope>
</dependency>
```
