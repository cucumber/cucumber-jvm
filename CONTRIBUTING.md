## About to contribute?

We appreciate that. Do keep the following in mind: 

* Before making significant contribution consider discussing the outline of 
  your solution first. This may avoid a duplication of efforts.
* When you send a [pull requests](https://help.github.com/articles/using-pull-requests), 
  please include tests to go along with it.
* Want to paste some code or output? Put \`\`\` on a line above and below your 
  code/output. See [GitHub Flavored Markdown](https://help.github.com/articles/github-flavored-markdown)'s 
  *Fenced Code Blocks* for details.

## Building

Cucumber-JVM is built with [Maven](http://maven.apache.org/) and includes a
[Maven Wrapper](https://maven.apache.org/wrapper) that will automatically
download a correct version of Maven.

When building the project for the first time, run:

```
./mvnw install -DskipTests=true -DskipITs=true -Darchetype.test.skip=true
```

The `cucumber-archetype` modules integration tests against `-SNAPSHOT` 
versions of Cucumber. These must be installed first.

Afterwards `./mvnw test` or `./mvnw verify` should work as expected.

## Formatting Java

The source code is formatted automatically by spotless when running:

```
./mvnw install
```

To configure IntelliJ IDEA/Eclipse use the configuration files in `.spotless/`.

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


