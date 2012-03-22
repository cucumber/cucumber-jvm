## [Git master](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC23...master)

* [Core] Understandable error message if a formatter needs output location. ([#148](https://github.com/cucumber/cucumber-jvm/issues/148), [#232](https://github.com/cucumber/cucumber-jvm/issues/232), [#269](https://github.com/cucumber/cucumber-jvm/issues/269) Aslak Hellesøy)
* [JUnit] Running with JUnit uses a null formatter by default (instead of a progress formatter). (Aslak Hellesøy)
* [Clojure] Fix release artifacts so cucumber-clojure can be released. ([#270](https://github.com/cucumber/cucumber-jvm/issues/270) Aslak Hellesøy)
* [Java] The @Pending annotation no longer exists. Throw a PendingException instead ([#271](https://github.com/cucumber/cucumber-jvm/issues/271) Aslak Hellesøy)

## [1.0.0.RC23](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC22...v1.0.0.RC23)

* [JUnit] CucumberException when running Cucumber with Jacoco code coverage ([#258](https://github.com/cucumber/cucumber-jvm/issues/258) Jan Stamer, Aslak Hellesøy)
* [Scala] Scala Javadoc problems with build ([#231](https://github.com/cucumber/cucumber-jvm/issues/231) Aslak Hellesøy)

## [1.0.0.RC22](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC21...v1.0.0.RC22)

* [Java] Snippets for DataTable include a hint about using List<YourType>, so people discover this neat technique (Aslak Hellesøy)
* [Core] Support DocString and DataTable in generated snippets ([#227](https://github.com/cucumber/cucumber-jvm/issues/227) Aslak Hellesøy)
* [Core] Fix broken --tags option (and get rid of JCommander for CLI parsing). ([#266](https://github.com/cucumber/cucumber-jvm/issues/266) Aslak Hellesøy)
* [Clojure] Make Clojure DSL syntax cleaner ([#244](https://github.com/cucumber/cucumber-jvm/issues/244) [#267](https://github.com/cucumber/cucumber-jvm/pull/267) rplevy-draker)
* [Clojure] Native Clojure backend ([#138](https://github.com/cucumber/cucumber-jvm/pull/138) [#265](https://github.com/cucumber/cucumber-jvm/pull/265) Kevin Downey, Nils Wloka)
* [JUnit] Added `format` attribute to `@Cucumber.Options` (Aslak Hellesøy)

## [1.0.0.RC21](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC20...v1.0.0.RC21)

* [Core] Ignore duplicate features instead of throwing exception. ([#259](https://github.com/cucumber/cucumber-jvm/issues/259) Aslak Hellesøy)
* [Core] Wrong message when runner on a non existing tag on feature ([#245](https://github.com/cucumber/cucumber-jvm/issues/245) Aslak Hellesøy, Jérémy Goupil)
* [Groovy, JRuby, Rhino] Make sure UTF-8 encoding is used everywhere ([#251](https://github.com/cucumber/cucumber-jvm/issues/251) Aslak Hellesøy)
* [Core, Cloure] Fixed StepDefinitionMatch to work with StepDefinitions that return null for getParameterTypes ([#250](https://github.com/cucumber/cucumber-jvm/issues/250), [#255](https://github.com/cucumber/cucumber-jvm/pull/255) Nils Wloka)
* [Java] Open up the `JavaBackend` API to ease integration from other tools ([#257](https://github.com/cucumber/cucumber-jvm/pull/257) Aslak Hellesøy).
* [Java] Inheritance in glue classes (stepdefs and hooks) is no longer supported - it causes too many problems. (Aslak Hellesøy).
* [JUnit] `@Cucumber.Options` annotation replaces `@Feature` annotation ([#160](https://github.com/cucumber/cucumber-jvm/issues/160) Aslak Hellesøy)
* [Spring] Slow Spring context performance ([#241](https://github.com/cucumber/cucumber-jvm/issues/241), [#242](https://github.com/cucumber/cucumber-jvm/pull/242) Vladimir Klyushnikov)
* [Core] Support for java.util.Calendar arguments in stepdefs. (Aslak Hellesøy)

## [1.0.0.RC20](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC16...v1.0.0.RC20)

* [JUnit] Improved JUnit runner. ([#107](https://github.com/cucumber/cucumber-jvm/issues/107), [#211](https://github.com/cucumber/cucumber-jvm/issues/211), [#216](https://github.com/cucumber/cucumber-jvm/pull/216) Giso Deutschmann)
* [Core] Stacktrace filtering filters away too much. ([#228](https://github.com/cucumber/cucumber-jvm/issues/228) Aslak Hellesøy)
* [Groovy] Fix native Groovy cucumber CLI ([#212](https://github.com/cucumber/cucumber-jvm/issues/212) Martin Hauner)
* [Core] Indeterministic feature ordering on Unix ([#224](https://github.com/cucumber/cucumber-jvm/issues/224) hutchy2570)
* [JUnit] New JUnitFormatter (--format junit) that outputs Ant-style JUnit XML. ([#226](https://github.com/cucumber/cucumber-jvm/pull/226), (#171)[https://github.com/cucumber/cucumber-jvm/issues/171] Vladimir Miguro)

## [1.0.0.RC16](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC15...v1.0.0.RC16)

* [Core] Embed text and images in reports. ([#205](https://github.com/cucumber/cucumber-jvm/issues/205) Aslak Hellesøy)
* [Core] Detect duplicate step definitions. (Aslak Hellesøy)
* [Java] Auto-generated step definitions should escape dollar signs / other regex chars ([#204](https://github.com/cucumber/cucumber-jvm/issues/204), [#215](https://github.com/cucumber/cucumber-jvm/pull/215) Ian Dees)
* [Core] Scenario Outlines work with tagged hooks. ([#209](https://github.com/cucumber/cucumber-jvm/issues/209), [#210](https://github.com/cucumber/cucumber-jvm/issues/210) Aslak Hellesøy)
* [Spring] Allowed customization of Spring step definitions context ([#203](https://github.com/cucumber/cucumber-jvm/pull/203) Vladimir Klyushnikov)
* [Core] Ambiguous step definitions don't cause Cucumber to blow up, they just fail the step. (Aslak Hellesøy)
* [Java] Fixed NullPointerException in ClasspathMethodScanner ([#201](https://github.com/cucumber/cucumber-jvm/pull/201) Vladimir Klyushnikov)
* [Groovy] Compiled Groovy stepdef scripts are found as well as source ones (Aslak Hellesøy)
* [Jython] I18n translations for most languages. Languages that can't be transformed to ASCII are excluded. ([#176](https://github.com/cucumber/cucumber-jvm/issues/176), [#197](https://github.com/cucumber/cucumber-jvm/pull/197) Stephen Abrams)

## [1.0.0.RC15](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC14...v1.0.0.RC15)

* [Java] You must use `cucumber.runtime.xstream` instead of `com.thoughtworks.xstream` for custom converters.
* [Core] XStream and Diffutils are now packaged inside the cucumber-core jar under new package names. ([#179](https://github.com/cucumber/cucumber-jvm/issues/179) Aslak Hellesøy)
* [Core] Fail if no features are found ([#163](https://github.com/cucumber/cucumber-jvm/issues/163) Aslak Hellesøy)
* [Core] Fail if duplicate features are detected ([#165](https://github.com/cucumber/cucumber-jvm/issues/165) Aslak Hellesøy)

## [1.0.0.RC14](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC13...v1.0.0.RC14)

* [Core] HTML formatter produces invalid page if no features ([#191](https://github.com/cucumber/cucumber-jvm/issues/191) Paolo Ambrosio)
* [Core] i18n java snippets for undefined steps are always generated with @Given annotation ([#184](https://github.com/cucumber/cucumber-jvm/issues/184) Vladimir Klyushnikov)
* [JUnit] Enhanced JUnit Exception Reporting ([#185](https://github.com/cucumber/cucumber-jvm/pull/185) Klaus Bayrhammer)
* [Guice] Constructor dependency resolution causes errors in GuiceFactory ([#189](https://github.com/cucumber/cucumber-jvm/issues/189) Matt Nathan)

## [1.0.0.RC13](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC12...v1.0.0.RC13)

* [Clojure] Fixed hooks ([#175](https://github.com/cucumber/cucumber-jvm/pull/175) Ronaldo M. Ferraz)
* [Core] Properly flush and close formatters ([#173](https://github.com/cucumber/cucumber-jvm/pull/173) Aslak Hellesøy, David Kowis)
* [Core] Use Gherkin's internal Gson (Aslak Hellesøy)
* [JUnit] Better reporting of Before and After blocks (Aslak Hellesøy)
* [Core] Bugfix: Scenario Outlines failing ([#170](https://github.com/cucumber/cucumber-jvm/issues/170) David Kowis, Aslak Hellesøy)
* [OpenEJB] It's back (was excluded from previous releases because it depended on unreleased libs). (Aslak Hellesøy)

## [1.0.0.RC12](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC11...v1.0.0.RC12)

* [JUnit] Tagged hooks are executed properly (Aslak Hellesøy)
* [JRuby] Better support for World blocks ([#166](https://github.com/cucumber/cucumber-jvm/pull/166) David Kowis)
* [Java] GluePath can be a package name ([#164](https://github.com/cucumber/cucumber-jvm/issues/164) Aslak Hellesøy)
* [Build] Fixed subtle path issues on Windows
* [Build] Fixed Build Failure: Cucumber-JVM: Scala (FAILURE) ([#167](https://github.com/cucumber/cucumber-jvm/issues/167) Aslak Hellesøy)

## [1.0.0.RC11](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC6...v1.0.0.RC11)

* [Build] The build is Maven-based again. It turned out to be the best choice.
* [Scala] The Scala module is back to life. ([#154](https://github.com/cucumber/cucumber-jvm/issues/154) Jon-Anders Teigen)
* [Build] The build should work on Windows again. ([#154](https://github.com/cucumber/cucumber-jvm/issues/154) Aslak Hellesøy)

## 1.0.0.RC6

* [Build] Maven pom.xml files are back (generated from ivy.xml). Ant+Ivy still needed for bootstrapping. 

## 1.0.0.RC5

* [Clojure] Snippets use single quote instead of double quote for comments.
* [All] Stepdefs in jars were not loaded correctly on Windows. ([#139](https://github.com/cucumber/cucumber-jvm/issues/139))
* [Build] Fixed repeated Ant builds. ([#141](https://github.com/cucumber/cucumber-jvm/issues/141))
* [Build] Push to local maven repo. ([#143](https://github.com/cucumber/cucumber-jvm/issues/143))

## 1.0.0.RC4

* [Build] Fixed transitive dependencies in POM files. ([#140](https://github.com/cucumber/cucumber-jvm/issues/140))
* [Build] Use a dot (not a hyphen) in RC version names. Required for JRuby gem.
* [Build] Started tagging repo after release.

## 1.0.0-RC3

* First proper release
