# Changelog
This file documents all notable changes for v2.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

----
## [2.4.0](https://github.com/cucumber/cucumber-jvm/compare/v2.3.1...v2.4.0)

### Added
 * [JUnit] Add readme to cucumber-junit ([#1306](https://github.com/cucumber/cucumber-jvm/pull/1306) M.P. Korstanje)
 * [Java] Add detail to DataTable hint in JavaSnippet ([#1298](https://github.com/cucumber/cucumber-jvm/pull/1298) Marit van Dijk)

### Changed
 * [Java] Remove 'throws Exception' from JavaSnippet ([#1308](https://github.com/cucumber/cucumber-jvm/pull/1308) Marit van Dijk)

### Fixed
 * [Android] Make test names passed to the instrumentation unique ([#1094](https://github.com/cucumber/cucumber-jvm/pull/1094) Christian Gnüchtel)
 * [Core] Json Formatter: include the content type of doc strings ([#1309](https://github.com/cucumber/cucumber-jvm/pull/1309) Björn Rasmusson)

## [2.3.1] (2017-12-14)

### Fixed
 * [Core] Remove scenario scoped step definitions from step definition cache ([#1301](https://github.com/cucumber/cucumber-jvm/pull/1301) M.P. Korstanje)

## [2.3.0] - [Release Announcement](release-notes/v2.3.0.md)

### Added
 * [Core] Cache matched steps definitions ([#1289](https://github.com/cucumber/cucumber-jvm/pull/1289) Łukasz Suski)

## [2.2.0] - [Release Announcement](release-notes/v2.2.0.md)

### Added
 * [JUnit] Document supported JUnit annotations ([#1272](https://github.com/cucumber/cucumber-jvm/pull/1272) Marit van Dijk)

### Changed
 * [Core] Upgraded tag-expressions to 1.1.1 (M.P. Korstanje)
   * Allows empty tag expressions ([cucumber/#296](https://github.com/cucumber/cucumber/issues/296))
   * Don't allow reverse polish notation in tag expressions ([cucumber/#304](https://github.com/cucumber/cucumber/issues/304))

### Deprecated
 * [JUnit] `Cucumber.createRuntime` has been deprecated ([#1287](https://github.com/cucumber/cucumber-jvm/pull/1287) M.P. Korstanje)

### Fixed
 * [Core] Add missing feature tags in the JSON output ([#1288](https://github.com/cucumber/cucumber-jvm/pull/1284) Pierre Gentile)
 * [Core] Fix detection of XStreamsConverters annotation ([#1283](https://github.com/cucumber/cucumber-jvm/pull/1283), [#1284](https://github.com/cucumber/cucumber-jvm/pull/1284) Dmitrii Demin)
 * [TestNG] Fix null pointer exception when invalid options are used ([#1282](https://github.com/cucumber/cucumber-jvm/pull/1282) M.P. Korstanje)

## [2.1.0] - [Release Announcement](release-notes/v2.1.0.md)

### Added
 * [JUnit] Print JUnit Options when unknown option is provided ([#1273](https://github.com/cucumber/cucumber-jvm/pull/1273), Marit Van Dijk)
 * [Spring] Support BootstrapWith annotation ([#1245](https://github.com/cucumber/cucumber-jvm/pull/1245), [#1242](https://github.com/cucumber/cucumber-jvm/pull/1242), [#1061](https://github.com/cucumber/cucumber-jvm/pull/1061) M.P. Korstanje)
 * [Core] Allow String parameter in plugin constructors (Aslak Hellesøy)
 * [Core] Prefer single-arg constructors over empty constructors in plugins ([#1104](https://github.com/cucumber/cucumber-jvm/issues/1104), [c6e471c2](https://github.com/cucumber/cucumber-jvm/commit/c6e471c27235fa3c091c6db1162c16291462a0ca) Aslak Hellesøy)
 * [Core] Optimize MethodScanner ([#1238](https://github.com/cucumber/cucumber-jvm/pull/1236) Łukasz Suski)
### Changed
 * [Core] Running empty Pickles yields the result undefined ([#1274](https://github.com/cucumber/cucumber-jvm/pull/1274) Björn Rasmusson)
 * [Core] Use gherkin 5.0.0 ([#1252](https://github.com/cucumber/cucumber-jvm/commit/5e305951026a1573ede77e05e86bbe8ed3bca55b) M.P. Korstanje)

### Deprecated
 * [Spring] Deprecate Spring context configuration by more than one class ([#1259](https://github.com/cucumber/cucumber-jvm/pull/1259) Björn Rasmusson)

### Removed
 * [Scala, Groovy, Clojure, Jython, JRuby, Rhino, Gosu] Moved to own repositories (M.P. Korstanje)

### Fixed
 * [Java8] Fix Java8StepDefinition.isDefinedA ([#1254](https://github.com/cucumber/cucumber-jvm/pull/1254), [#1255](https://github.com/cucumber/cucumber-jvm/pull/1255) tts-ll, M.P. Korstanje)
 * [Core] Fix race condition in Timeout ([#1244](https://github.com/cucumber/cucumber-jvm/pull/1244) M.P. Korstanje)
 * [Core] Correct the name of the Json Formatter embeddings node ([#1236](https://github.com/cucumber/cucumber-jvm/pull/1236) Haroon Sheikh)
 * [Spring] Exception is thrown complaining about multiple matching beans ([#1225](https://github.com/cucumber/cucumber-jvm/pull/1225), [#1226](https://github.com/cucumber/cucumber-jvm/pull/1226), M.P. Korstanje)

## [2.0.1] - [Release Announcement](release-notes/v2.0.1.md)

### Added
 * [Core] cucumber.api.TableConverter interface ([#1223](https://github.com/cucumber/cucumber-jvm/pull/1223) M.P. Korstanje)

### Deprecated
 * [Core] Deprecated constructors and run method of TestCase and Test Step ([#1223](https://github.com/cucumber/cucumber-jvm/pull/1223) M.P. Korstanje)

### Fixed
 * [Core] Skip test step execution if --dry-run is specified ([#1220](https://github.com/cucumber/cucumber-jvm/pull/1220) ,[#1219](https://github.com/cucumber/cucumber-jvm/issues/1219) Adrian Baker)
 * [Java8] NullPointerException at Java8StepDefinition.isDefinedAt ([#1222](https://github.com/cucumber/cucumber-jvm/pull/1222), [#1217](https://github.com/cucumber/cucumber-jvm/issues/1217) M.P. Korstanje)
 * [Core] Scenario.isFailed always return false ([#1216](https://github.com/cucumber/cucumber-jvm/pull/1216), [#1215](https://github.com/cucumber/cucumber-jvm/issues/1215) Olivier Lemasle)
 * [Docs] Javadoc stylesheet issue ([#1212](https://github.com/cucumber/cucumber-jvm/pull/1212), [#796](https://github.com/cucumber/cucumber-jvm/issues/796) Marit Van Dijk)

## [2.0.0] - [Release Announcement](release-notes/v2.0.0.md)

* [Java] Reduce Throwable to Exception in JavaSnippet ([#1207](https://github.com/cucumber/cucumber-jvm/issues/1207), [#1208](https://github.com/cucumber/cucumber-jvm/pull/1208) M.P. Korstanje)
* [Core] Update the cucumber-html dependency to version 0.2.6 (Björn Rasmusson)
* [Core] Fix PrettyFormatter exception on nested arguments ([#1200](https://github.com/cucumber/cucumber-jvm/pull/1200) Marit van Dijk, M.P. Korstanje)
* [Core] Added tests for diffing with empty table and list ([#1194](https://github.com/cucumber/cucumber-jvm/pull/1194) Marit van Dijk, M.P. Korstanje)
* [JUnit] Invoke (Before|After)Class and TestRules around Cucumber execution ([#1190](https://github.com/cucumber/cucumber-jvm/pull/1190) M.P. Korstanje)
* [Core] Use whole path for uri:s for file system feature files ([#1189](https://github.com/cucumber/cucumber-jvm/pull/1189), [#854](https://github.com/cucumber/cucumber-jvm/issues/854) Björn Rasmusson)
* [Java, Java8, Kotlin Java8] Support method references ([#1178](https://github.com/cucumber/cucumber-jvm/pull/1178), [#1140](https://github.com/cucumber/cucumber-jvm/pull/1140) M.P. Korstanje)
  * Java8 method references can be used in lambda step definitions
  * It is no longer possible to use lambda step definitions without also using `cucumber-java8`
  * Lambda step definitions can be used in Kotlin. Function references are not yet understood
* [Core] Make the parsing of the rerun file more robust ([#1187](https://github.com/cucumber/cucumber-jvm/pull/1187) M.P. Korstanje)
* [Android] Update the version of the cucumber-jvm-deps dependency - to a version without Java8 bytecode ([#1170](https://github.com/cucumber/cucumber-jvm/pull/1170), [#893](https://github.com/cucumber/cucumber-jvm/issues/893) Björn Rasmusson)
* [Needle] Handle circular dependencies ([#853](https://github.com/cucumber/cucumber-jvm/pull/853) Lars Bilger)
* [Core] Use "uri" instead of "path" to reference feature files in external APIs ([#1179](https://github.com/cucumber/cucumber-jvm/pull/1179) Björn Rasmusson)
* [Core] Separate rerun paths by a new line character ([#1177](https://github.com/cucumber/cucumber-jvm/pull/1177), [#1187](https://github.com/cucumber/cucumber-jvm/pull/1187) M.P. Korstanje)
* [TestNG] Run a separate TestNG test per scenario (deprecate one TestNG test per feature, and one TestNG for the whole suite) ([#1174](https://github.com/cucumber/cucumber-jvm/pull/1174), [#1113](https://github.com/cucumber/cucumber-jvm/issues/1113) Luciano van der Veekens, Björn Rasmusson)
* [Core] Close OutputStream for embedded images in HTML formatter ([#1175](https://github.com/cucumber/cucumber-jvm/pull/1175), [#1108](https://github.com/cucumber/cucumber-jvm/issues/1108) M.P. Korstanje)
* [Scala] Compile cucumber-scala_2.12 against Java 8 ([#1171](https://github.com/cucumber/cucumber-jvm/pull/1171), [#1087](https://github.com/cucumber/cucumber-jvm/issues/1087) M.P. Korstanje, Paolo Ambrosio). This includes:
  * Update Scala Versions
    - 2.12.0-M1 to 2.12.2
    - 2.11.8 to 2.11.11
  * Use Manifest instead of Java reflection to provide type information
* [Core] Avoid closing System.out or System.err from formatters ([#1173](https://github.com/cucumber/cucumber-jvm/issues/1173) Björn Rasmusson)
* [Core] Decouple UndefinedStepsTracker from Glue ([#1019](https://github.com/cucumber/cucumber-jvm/pull/1019) [#1172](https://github.com/cucumber/cucumber-jvm/pull/1172) Illapikov, M.P. Korstanje)
* [Core] Add TestRunStarted event, let Stats handle the exit code ([#1162](https://github.com/cucumber/cucumber-jvm/pull/1162) Björn Rasmusson)
* [Core, JUnit, Android] Add the ambiguous result type ([#1168](https://github.com/cucumber/cucumber-jvm/pull/1168) Björn Rasmusson)
* [Core] Add the SnippetsSuggestedEvent ([#1163](https://github.com/cucumber/cucumber-jvm/pull/1163) Björn Rasmusson)
* [Java] Prevent MethodScanner from checking Object.class methods ([#940](https://github.com/cucumber/cucumber-jvm/pull/940) Łukasz Suski)
* [Weld] Use Weld SE 2.4.4.Final ([#1166](https://github.com/cucumber/cucumber-jvm/pull/1166) Frank Seidinger)
* [Core] Provide a unique id of the current scenario to the hooks. ([#1160](https://github.com/cucumber/cucumber-jvm/pull/1160) Björn Rasmusson)
* [Gosu] Fix and re-enable Gosu for 2.0.0 ([#1155](https://github.com/cucumber/cucumber-jvm/pull/1155), [#1086](https://github.com/cucumber/cucumber-jvm/pull/1086), [#874](https://github.com/cucumber/cucumber-jvm/pull/874) Kyle Moore, M.P. Korstanje)
* [Core] Fix issue where ComplexTypeWriter would create unbalanced tables. ([#1042](https://github.com/cucumber/cucumber-jvm/pull/1042) Roy Jacobs, M.P. Korstanje)
* [Guice] Use the ContextClassLoader when loading InjectorSource. ([#1036](https://github.com/cucumber/cucumber-jvm/pull/1036), [#1037](https://github.com/cucumber/cucumber-jvm/pull/1037) Kyle Moore)
* [Core] Allow global registration of custom XStream converters. ([#1010](https://github.com/cucumber/cucumber-jvm/pull/1010), [#1009](https://github.com/cucumber/cucumber-jvm/issues/1009) Chris Rankin)
* [Spring] Support multithreaded execution of scenarios ([#1106](https://github.com/cucumber/cucumber-jvm/issues/1106), [#1107](https://github.com/cucumber/cucumber-jvm/issues/1107), [#1148](https://github.com/cucumber/cucumber-jvm/issues/1148), [#1153](https://github.com/cucumber/cucumber-jvm/pull/1153) Ismail Bhana, M.P. Korstanje)
* [Core] Show explicit error message when field name missed in table header ([#1014](https://github.com/cucumber/cucumber-jvm/pull/1014) Mykola Gurov)
* [Examples] Properly quit selenium in webbit examples ([#1146](https://github.com/cucumber/cucumber-jvm/pull/1146) Alberto Scotto)
* [JUnit] Use AssumptionFailed to mark scenarios/steps as skipped ([#1142](https://github.com/cucumber/cucumber-jvm/pull/1142) Björn Rasmusson)
* [Core] Map AssumptionViolatedException to Skipped status ([#1145](https://github.com/cucumber/cucumber-jvm/pull/1145), [#1007](https://github.com/cucumber/cucumber-jvm/issues/1007) Björn Rasmusson)
* [Java] SnippetGenerator recognises parameters from Scenario Outline ([#1078](https://github.com/cucumber/cucumber-jvm/pull/1078) Andrey Vokin)
* [Java8] Allow lambda steps to throw checked Exceptions ([#1001](https://github.com/cucumber/cucumber-jvm/issues/1001), [#1110](https://github.com/cucumber/cucumber-jvm/pull/1110) Christian Hujer)
* [JUnit] Add `--[no-]step-notifications` option to JunitOptions (no step notifications is the default) ([#1135](https://github.com/cucumber/cucumber-jvm/pull/1135), [#1159](https://github.com/cucumber/cucumber-jvm/pull/1159), [#263](https://github.com/cucumber/cucumber-jvm/issues/263), [#935](https://github.com/cucumber/cucumber-jvm/issues/935), [#577](https://github.com/cucumber/cucumber-jvm/issues/577) M.P. Korstanje, Björn Rasmusson)
* [JUnit] Use deterministic unique ids in Descriptions ([#1134](https://github.com/cucumber/cucumber-jvm/pull/1134), [#1120](https://github.com/cucumber/cucumber-jvm/issues/1120) mpkorstanje)
* [All] Support [Tag Expressions](https://github.com/cucumber/cucumber/tree/master/tag-expressions) (part of [#1035](https://github.com/cucumber/cucumber-jvm/pull/1035) Björn Rasmusson)
* [All] Upgrade to Gherkin 4.1 ([#1035](https://github.com/cucumber/cucumber-jvm/pull/1035), [#1131](https://github.com/cucumber/cucumber-jvm/pull/1131), [#1133](https://github.com/cucumber/cucumber-jvm/pull/1133) Björn Rasmusson, M.P. Korstanje). This also fixes:
  * JsonFormatter attach text to last step when sceneario.write is invoked from after hook ([#1080](https://github.com/cucumber/cucumber-jvm/issues/1080))
  * CucumberOptions: Tags and name do not work well together ([#976](https://github.com/cucumber/cucumber-jvm/issues/976))
  * Tags at the examples block are not treated as actual tags in scenario ([#849](https://github.com/cucumber/cucumber-jvm/issues/849))
  * NullPointerException from @Before tag ([#638](https://github.com/cucumber/cucumber-jvm/issues/638), [#701](https://github.com/cucumber/cucumber-jvm/issues/701))
* [All] Change the maven groupId to io.cucumber (part of [#1035](https://github.com/cucumber/cucumber-jvm/pull/1035) Björn Rasmusson)

<!-- Releases -->
[2.4.0]:      https://github.com/cucumber/cucumber-jvm/compare/v2.3.1...v2.4.0
[2.3.1]:      https://github.com/cucumber/cucumber-jvm/compare/v2.3.0...v2.3.1
[2.3.0]:      https://github.com/cucumber/cucumber-jvm/compare/v2.2.0...v2.3.0
[2.2.0]:      https://github.com/cucumber/cucumber-jvm/compare/v2.1.0...v2.2.0
[2.1.0]:      https://github.com/cucumber/cucumber-jvm/compare/v2.0.1...v2.1.0
[2.0.1]:      https://github.com/cucumber/cucumber-jvm/compare/v2.0.0...v2.0.1
[2.0.0]:      https://github.com/cucumber/cucumber-jvm/compare/v1.2.5...v2.0.0
