## About to create a new Github Issue?

We appreciate that. But before you do, please learn our basic rules:

* This is not a support or discussion forum. If you have a question, please ask it on [The Cukes Google Group](http://groups.google.com/group/cukes).
* Do you have a feature request? Then don't expect it to be implemented unless you or someone else sends a [pull request](https://help.github.com/articles/using-pull-requests).
* Reporting a bug? We need to know what java/ruby/node.js etc. runtime you have, and what jar/gem/npm package versions you are using. Bugs with [pull requests](https://help.github.com/articles/using-pull-requests) get fixed quicker. Some bugs may never be fixed.
* You have to tell us how to reproduce a bug. Bonus point for a [pull request](https://help.github.com/articles/using-pull-requests) with a failing test that reproduces the bug.
* Want to paste some code or output? Put \`\`\` on a line above and below your code/output. See [GFM](https://help.github.com/articles/github-flavored-markdown)'s *Fenced Code Blocks* for details.
* We love [pull requests](https://help.github.com/articles/using-pull-requests), but if you don't have a test to go with it we probably won't merge it.

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

Your `.feature` files must be in a folder that IDEA recognises as *source* or *test*. You must also tell IDEA to copy your `.feature` files to your output directory:

```
Preferences -> Compiler -> Resource Patterns -> Add `;?*.feature`
```

If you are writing step definitions in a scripting language you must also add the appropriate file extension for that language as well.

### Eclipse

Just load the root `pom.xml`

## Contributing/Hacking

To hack on Cucumber-JVM you need a JDK, Maven and Git to get the code. You also need to set your IDE/text editor to use:

* UTF-8 file encoding <sup>+</sup>
* LF (UNIX) line endings <sup>+</sup>
* No wildcard imports
* Curly brace on same line as block
* 4 Space indent (no tabs) <sup>+</sup>
  * Java
  * XML
* 2 Space indent (no tabs) <sup>+</sup>
  * Gherkin

`+` These are set automatically if you use an editor/IDE that supports [EditorConfig](http://editorconfig.org/#download).

Please do *not* add @author tags - this project embraces collective code ownership. If you want to know who wrote some
code, look in git. When you are done, send a [pull request](http://help.github.com/send-pull-requests/).
If we get a pull request where an entire file is changed because of insignificant whitespace changes we cannot see what
you have changed, and your contribution might get rejected.

## Troubleshooting

Below are some common problems you might encounter while hacking on Cucumber-JVM - and solutions.

### IntelliJ Idea fails to compile the generated I18n Java annotations

This can be solved by changing the Compiler settings: `Preferences -> Compiler -> Java Compiler`:

* *Use compiler:* `Javac`
* *Additional command line parameters:* `-target 1.6 -source 1.6 -encoding UTF-8`

Note that even though development is sometimes easier to do with 1.6, releasing should be done with 1.7.

## Releasing

Upload privileges to the Sonatype staging repository and owner rights to the cucumber-jvm gem at RubyGems.org are required.

First, make sure everything builds. Including Android.

Then, see if you can upgrade any dependencies:

```
mvn versions:display-dependency-updates
```

This is a reminder to the developers:

Then, make sure you have the proper keys set up - in your `~/.m2/settings.xml` - for example:

```
<settings>
  <servers>
    <server>
      <id>cukes.info</id>
      <username>yourcukesinfouser</username>
      <privateKey>fullkeypath</privateKey>
    </server>
    <!-- See https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide -->
    <server>
      <id>sonatype-nexus-snapshots</id>
      <username>yoursonatypeuser</username>
      <password>TOPSECRET</password>
    </server>
    <server>
      <id>sonatype-nexus-staging</id>
      <username>yoursonatypeuser</username>
      <password>TOPSECRET</password>
    </server>
  </servers>
</settings>
```

Replace version numbers in:

* examples/java-gradle/build.gradle
* examples/android/android-studio/Cukeulator/app/build.gradle
* examples/clojure_cukes/project.clj
* CHANGELOG.md

Make sure you can generate Javadocs for all modules, or else the
release will fail:

```
mvn javadoc:javadoc
```

Run `git commit -am "Prepare for release X.Y.Z"`

Now release everything (replace X.Y.Z below with the next release number):

```
mvn release:clean
mvn --batch-mode -P release-sign-artifacts release:prepare -DautoVersionSubmodules=true -DdevelopmentVersion=X.Y.Z-SNAPSHOT
mvn -P release-sign-artifacts release:perform
```

Update the pom.xml file for the examples/android modules (which are not automatically updated by the release process), commit and push.

Post release the API docs must be generated for each module and manually copied over to a working copy of the [api.cucumber.io](https://github.com/cucumber/api.cucumber.io) which must be a sibling of `cucumber-jvm` (this repo):

```
./doc/genapi.sh VERSION
```

After that's done, commit and push `api.cucumber.io`

Now, update the dependency in example projects:

* https://github.com/cucumber/cucumber-java-skeleton

This technique to collect coverage for a multi-module Maven project is based on a
[blog post](http://thomassundberg.wordpress.com/2012/02/18/test-coverage-in-a-multi-module-maven-project/) by Thomas Sundberg.
