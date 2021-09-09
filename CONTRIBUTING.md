## About to contribute?

We appreciate that. Do keep the following in mind: 

* Before making significant contribution consider discussing the outline of 
  your solution first. This may avoid a duplication of efforts.
* When you send a [pull requests](https://help.github.com/articles/using-pull-requests), 
  please include tests to go along with it.
* Want to paste some code or output? Put \`\`\` on a line above and below your 
  code/output. See [Github Flavored Markdown](https://help.github.com/articles/github-flavored-markdown)'s 
  *Fenced Code Blocks* for details.

## Formatting Java

To automatically format the java source code run

```
mvn spotless:apply
```

To configure Intelij IDEA/Eclipse use the configuration files in `.spotless/`.

## Formatting XML, Gherkin, ect

* UTF-8 file encoding <sup>+</sup>
* LF (UNIX) line endings <sup>+</sup>
* 4 Space indent (no tabs) <sup>+</sup>
  * XML
  * Java
* 2 Space indent (no tabs) <sup>+</sup>
  * Gherkin

`+` These are set automatically if you use an editor/IDE that supports 
[EditorConfig](http://editorconfig.org/#download).

## Building Cucumber-JVM

Cucumber-JVM is built with [Maven](http://maven.apache.org/).

```
mvn clean install
```

## IDE Setup

### IntelliJ IDEA

```
File -> Open Project -> path/to/cucumber-jvm/pom.xml
```

Your `.feature` files must be in a folder that IDEA recognises as *source* or 
*test*. You must also tell IDEA to copy your `.feature` files to your output 
directory:

```
Preferences -> Compiler -> Resource Patterns -> Add `;?*.feature`
```

If you are writing step definitions in a scripting language you must also add 
the appropriate file extension for that language as well.

### Eclipse

Just load the root `pom.xml`
