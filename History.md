## [1.0.11](https://github.com/cucumber/cucumber-jvm/compare/v1.0.10...v1.0.11)

* [Core] Added a new `@Transform` annotation and an abstract `Transformer` class giving full control over argument transforms.
* [OpenEJB] Remove log4j need for openejb module ([#355](https://github.com/cucumber/cucumber-jvm/pull/355) rmannibucau)
* [JUnit] JUnit report doesn't correctly report errors ([#315](https://github.com/cucumber/cucumber-jvm/issues/315), [#356](https://github.com/cucumber/cucumber-jvm/pull/356) Kevin Cunningham)

## [1.0.10](https://github.com/cucumber/cucumber-jvm/compare/v1.0.9...v1.0.10)

* [Core] Automatically convert data tables to lists of enums just as is done with classes [#346](https://github.com/cucumber/cucumber-jvm/issues/346)
* [Core] `DataTable.create()` and `TableConverter.toTable()` will omit columns for object fields that are null, *unless columns are explicitly listed*. See [#320](https://github.com/cucumber/cucumber-jvm/pull/320) (Aslak Hellesøy)
* [Core] Table conversion to `List<Map>` converts to a List of Map of String to String. (Aslak Hellesøy)
* [Core] Table conversion to `List<Map<KeyType,ValueType>>` works for enums, dates, strings and primitives. (Aslak Hellesøy)
* [Core] Formatters should report feature paths as relative paths. ([#337](https://github.com/cucumber/cucumber-jvm/issues/337), [#342](https://github.com/cucumber/cucumber-jvm/pull/342) mattharr)
* [Java/Groovy] Step definitions and hooks can now specify a timeout (milliseconds) after which a `TimeoutException` is thrown if the stepdef/hook has not completed.
  Please note that for Groovy, `sleep(int)` is not interruptible, so in order for sleeps to work your code must use `Thread.sleep(int)` ([#343](https://github.com/cucumber/cucumber-jvm/issues/343) Aslak Hellesøy)
* [Java] More explanatary exception if a hook is declared with bad parameter types. (Aslak Hellesøy)
* [Core/JUnit] JUnit report has time reported as seconds instead of millis. ([#347](https://github.com/cucumber/cucumber-jvm/issues/347) Aslak Hellesøy)
* [Core] List legal enum values if conversion fails ([#344](https://github.com/cucumber/cucumber-jvm/issues/344) Aslak Hellesøy)
* [Weld] Added workaround for [WELD-1119](https://issues.jboss.org/browse/WELD-1119) when running on single core machines. (Aslak Hellesøy)

## [1.0.9](https://github.com/cucumber/cucumber-jvm/compare/v1.0.8...v1.0.9)

* [Core] Exceptions thrown from a step definition are no longer wrapped in CucumberException. (Aslak Hellesøy)
* [Core] Fixed regression: PendingException was causing steps to fail instead of pending. ([#328](https://github.com/cucumber/cucumber-jvm/issues/328) Aslak Hellesøy)
* [Java] Missing String.format parameters in DefaultJavaObjectFactory ([#336](https://github.com/cucumber/cucumber-jvm/issues/336) paulkrause88, Aslak Hellesøy)
* [Core] Exceptions being swallowed if reported in a Hook ([#133](https://github.com/cucumber/cucumber-jvm/issues/133) David Kowis, Aslak Hellesøy)
* [Core] Added `DataTable.asMaps()` and made all returned lists immutable. (Aslak Hellesøy).
* [Java] The java-helloworld example has a simple example illustrating data tables and doc strings. (Aslak Hellesøy).
* [Core] Run scenarios/features by name ([#233](https://github.com/cucumber/cucumber-jvm/issues/233), [#323](https://github.com/cucumber/cucumber-jvm/pull/323) Klaus Bayrhammer)
* [Jython] Added missing `self` argument in Jython snippets. ([#324](https://github.com/cucumber/cucumber-jvm/issues/324) Aslak Hellesøy)
* [Scala] Fixed regression from v1.0.6 in Scala module - glue code wasn't loaded at all. ([#321](https://github.com/cucumber/cucumber-jvm/issues/321) Aslak Hellesøy)

## [1.0.8](https://github.com/cucumber/cucumber-jvm/compare/v1.0.7...v1.0.8)

* [Core] Ability to create `DataTable` objects from a List of objects while specifying what header columns (fields) to use (Aslak Hellesøy)
* [Core] `table.diff(listOfPojos)` no longer spuriously fails because of pseudo-random column/field ordering (Aslak Hellesøy)
* [Core] Tables with empty cells make the column disappear ([#320](https://github.com/cucumber/cucumber-jvm/pull/320) Aslak Hellesøy, Gilles Philippart)
* [Java] Add 'throws Throwable' to generated Java stepdef snippets ([#318](https://github.com/cucumber/cucumber-jvm/issues/318), [#319](https://github.com/cucumber/cucumber-jvm/pull/319) Petter Måhlén)
* [Core] Remove forced UTC timezone. ([#317](https://github.com/cucumber/cucumber-jvm/pull/317) Gilles Philippart)
* [Core] Options (Command line or `@Cucumber.Options`) can be overriden with the `cucumber.options` system property. (Aslak Hellesøy)

## [1.0.7](https://github.com/cucumber/cucumber-jvm/compare/v1.0.6...v1.0.7)

* [Java] cucumber-java lazily creates instances, just like the other DI containers. (Aslak Hellesøy)
* [Core] Throw an exception if a glue or feature path doesn't exist (i.e. neither file nor directory) (Aslak Hellesøy)

## [1.0.6](https://github.com/cucumber/cucumber-jvm/compare/v1.0.4...v1.0.6)

* [JUnit] Scenarios with skipped, pending or undefined steps show up as yellow in IDEA and Eclipse (They used to be green while the steps were yellow). (Aslak Hellesøy)
* [Core] Loading features and glue code from the `CLASSPATH` can be done with `classpath:my/path` ([#312](https://github.com/cucumber/cucumber-jvm/issues/312) Aslak Hellesøy)
* [Clojure] Clojure example can't find cuke_steps.clj ([#291](https://github.com/cucumber/cucumber-jvm/issues/291), [#309](https://github.com/cucumber/cucumber-jvm/pull/309) Nils Wloka)

## [1.0.4](https://github.com/cucumber/cucumber-jvm/compare/v1.0.3...v1.0.4)

* [Core] Ability to specify line numbers: `@Cucumber.Options(features = "my/nice.feature:2:10")` ([#234](https://github.com/cucumber/cucumber-jvm/issues/234) Aslak Hellesøy)
* [WebDriver] Improved example that shows how to reuse a driver for the entire JVM. (Aslak Hellesøy)
* [Core] Allow custom @XStreamConverter to be used on regular arguments - not just table arguments. (Aslak Hellesøy)
* [Groovy] fixed & simplified groovy step snippets ([#303](https://github.com/cucumber/cucumber-jvm/pull/303) Martin Hauner)
* [Java] Detect subclassing in glue code and report to the user that it's illegal. ([#301](https://github.com/cucumber/cucumber-jvm/issues/301) Aslak Hellesøy)
* [Core] Friendlier error message when XStream fails to assign null to primitive fields ([#296](https://github.com/cucumber/cucumber-jvm/issues/296) Aslak Hellesøy)

## [1.0.3](https://github.com/cucumber/cucumber-jvm/compare/v1.0.2...v1.0.3)

* [Core] Friendlier error message when XStream fails conversion ([#296](https://github.com/cucumber/cucumber-jvm/issues/296) Aslak Hellesøy)
* [Core] Empty strings from matched steps and table cells are converted to `null`. This means boxed types must be used if you intend to have empty strings. (Aslak Hellesøy)
* [Core] Implement --strict ([#196](https://github.com/cucumber/cucumber-jvm/issues/196), [#284](https://github.com/cucumber/cucumber-jvm/pull/284) Klaus Bayrhammer)
* [Clojure] Cucumber-clojure adding after hook to before ([#294](https://github.com/cucumber/cucumber-jvm/pull/294) Daniel E. Renfer)
* [Java] Show code source for Java step definitions in case of duplicates or ambiguous stepdefs. (Aslak Hellesøy).
* [Groovy] Arity mismatch can be avoided by explicitly declaring an empty list of closure parameters. ([#297](https://github.com/cucumber/cucumber-jvm/issues/297) Aslak Hellesøy)
* [Core] Added DataTable.toTable(List<?> other) for creating a new table. Handy for printing a table when diffing isn't helpful. (Aslak Hellesøy)

## [1.0.2](https://github.com/cucumber/cucumber-jvm/compare/v1.0.1...v1.0.2)

* [Java] Snippets using a table have a hint about how to use List<YourClass>. (Aslak Hellesøy)
* [Java] Don't convert paths to package names - instead throw an exception. This helps people avoid mistakes. (Aslak Hellesøy)
* [Scala] Fixed generated Scala snippets ([#282](https://github.com/cucumber/cucumber-jvm/pull/282) pawel-s)
* [JUnit] Automatically turn off ANSI colours when launched from IDEA. (Aslak Hellesøy)

## [1.0.1](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0...v1.0.1)

* [Clojure] Fix quoting of generated Clojure snippets ([#277](https://github.com/cucumber/cucumber-jvm/pull/277) Michael van Acken)
* [Guice] Guice in multi module/class loader setup ([#278](https://github.com/cucumber/cucumber-jvm/pull/278) Matt Nathan)
* [JUnit] Background steps show up correctly in IntelliJ ([#276](https://github.com/cucumber/cucumber-jvm/issues/276) Aslak Hellesøy)

## [1.0.0](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC24...v1.0.0)

* [Docs] Added Cuke4Duke migration notes to README ([#239](https://github.com/cucumber/cucumber-jvm/pull/239) coldbloodedtx)
* [Core] Added --monochrome flag, allowing monochrome output for certain formatters ([#221](https://github.com/cucumber/cucumber-jvm/issues/221) Aslak Hellesøy)
* [Core] Added a usage formatter ([#207](https://github.com/cucumber/cucumber-jvm/issues/207), [#214](https://github.com/cucumber/cucumber-jvm/pull/214) Klaus Bayrhammer)
* [Core] JavaScript-Error in HTML-Report when using ScenarioResult.write ([#254](https://github.com/cucumber/cucumber-jvm/issues/254) Aslak Hellesøy)
* [Java] Add support for enums in stepdefs ([#217](https://github.com/cucumber/cucumber-jvm/issues/217), [#240](https://github.com/cucumber/cucumber-jvm/pull/240) Gilles Philippart)
* [Core] Help text for CLI. ([#142](https://github.com/cucumber/cucumber-jvm/issues/142) Aslak Hellesøy)
* [JUnit] Eclipse JUnit reports inaccurate run count ([#263](https://github.com/cucumber/cucumber-jvm/issues/263), [#274](https://github.com/cucumber/cucumber-jvm/pull/274) dgradl)

## [1.0.0.RC24](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC23...v1.0.0.RC24)

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
* [JUnit] New JUnitFormatter (--format junit) that outputs Ant-style JUnit XML. ([#226](https://github.com/cucumber/cucumber-jvm/pull/226), [#171](https://github.com/cucumber/cucumber-jvm/issues/171) Vladimir Miguro)

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
