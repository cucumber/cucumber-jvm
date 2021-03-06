# Bill of Materials

There are two ways to use the Bill of Materials (BOM) pom.
 
Set as a parent pom:

```xml
<parent>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-bom</artifactId>
    <version>6.10.1</version>
</parent>
```

Or add into the `<dependencyManagement>` section:

```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-bom</artifactId>
    <version>${cucumber.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

Two approaches are same with respect to dependency inclusion, usually latter is preferable.
