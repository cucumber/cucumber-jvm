# Changelog
This file documents all notable changes for v4.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

----
## [4.8.1] (2020-01-09)

### Fixed
* [JUnit] Fix JUnit v4.13 runtime issues ([#1852](https://github.com/cucumber/cucumber-jvm/issues/1794) John Patrick)

## [4.8.0] (2019-10-19)

### Fixed
* [Core] Update html report jQuery from 1.8.2 to 3.4.1 (#1794)
   ([#1794](https://github.com/cucumber/cucumber-jvm/issues/1794) A. Dale Clarke)

### Deprecated
* [Java] Deprecate `timout` in favour of library based solutions ([#1506](https://github.com/cucumber/cucumber-jvm/issues/1506), [#1694](https://github.com/cucumber/cucumber-jvm/issues/1694) M.P. Korstanje)
   - Prefer using library based solutions
    * [JUnit 5 `Assertions.assertTimeout*`](https://junit.org/junit5/docs/5.0.1/api/org/junit/jupiter/api/Assertions.html#assertTimeout-java.time.Duration-org.junit.jupiter.api.function.Executable-)
    * [Awaitility](https://github.com/awaitility/awaitility)
    * [Guava `TimeLimiter`](https://github.com/google/guava/blob/master/guava/src/com/google/common/util/concurrent/TimeLimiter.java)


## [4.7.4] (2019-10-05)

### Fixed
*  [Core] Do not clobber line filters from rerun file ([#1789](https://github.com/cucumber/cucumber-jvm/pull/1789) M.P. Korstanje, Malte Engels)

## [4.7.3] (2019-10-04)

### Fixed
* [Core] Upgrade the timeline formatter's jQuery dependency from 3.3.1 to 3.4.1. jQuery 3.3.1 has an [XSS vulnerability](https://www.cvedetails.com/cve/CVE-2019-11358/)
   that wouldn't normally affect the timeline formatter. However, it did prevent some organisations from downloading the cucumber-core jar because nexus would block it.
   ([#1759](https://github.com/cucumber/cucumber-jvm/issues/1759), [#1769](https://github.com/cucumber/cucumber-jvm/pull/1769), [#1786](https://github.com/cucumber/cucumber-jvm/issues/1786) Vincent Pretre, Aslak Hellesøy, M.P. Korstanje)

## [4.7.2] (2019-08-28)

### Fixed
 * [JUnit] JUnit will always print undefined steps (M.P. Korstanje)
 * [TestNG] TestNG will always print undefined steps (M.P. Korstanje)

## [4.7.1] (2019-07-28)

### Fixed
  *  [All] Add missing JPMS config ([#1709](https://github.com/cucumber/cucumber-jvm/pull/1709)  John Patrick)
    - Add automatic module name for `core`
    - Add automatic module name for `cd2`
    - Add automatic module name for `guice`

## [4.7.0] (2019-07-20)

### Added
 * [Core] Add property to select io.cucumber.core.backend.ObjectFactory implementation  ([#1700](https://github.com/cucumber/cucumber-jvm/pull/1700) Ralph Kar)
    - Use `cucumber.object-factory=com.example.CustomObjectFactory` in either `cucucmber.properties`,
      environment variables, or system properties

## [4.6.0] (2019-07-16)

### Added
 * [Core] Allow to add names for embeddings ([#1692](https://github.com/cucumber/cucumber-jvm/pull/1693)  Dzieciak)

## [4.5.4] (2019-07-10)

### Fixed
 * [Core] Restore Scenario#getSourceTagNames() ([#1689](https://github.com/cucumber/cucumber-jvm/pull/1689)  Tommy Wo)

## [4.5.3] (2019-07-07)

### Fixed
 * [Core] Reference correct main class in deprecation warning (M.P. Korstanje)

## [4.5.2] (2019-07-02)

### Fixed
 * [Java] Fix ClassCastException on new Transpose annotation ([#1683](https://github.com/cucumber/cucumber-jvm/pull/1683)  Geoffroy Van Elsuve)

## [4.5.1] (2019-07-01)

### Fixed
 * [Java] Fix link to new steps in generated Java doc ([#1681](https://github.com/cucumber/cucumber-jvm/pull/1681) M.P. Korstanje)

## [4.5.0] (2019-06-30)

### Changed
 * [JUnit] JUnit will no longer run in verbose mode by default ([#1670](https://github.com/cucumber/cucumber-jvm/pull/1670) M.P. Korstanje)
    - Add `summary` and/or `progress` plugins to restore output
 * [TestNG] TestNG will no longer run in verbose mode by default ([#1670](https://github.com/cucumber/cucumber-jvm/pull/1670) M.P. Korstanje)
    - Add `summary` and/or `progress` plugins to restore output
 * [Java] Use ServiceLoader for Guice, Needle, OpenEJB, Pico, Spring and Weld `ObjectFactory` implementations.
    - Removes spurious deprecation warning.
    - Moves `ObjectFactory` implements to `io.cucumber.<module-name>` package.

### Deprecated
 * [Core] Deprecate `cucumber.api.CucumberOptions` ([#1670](https://github.com/cucumber/cucumber-jvm/pull/1670) M.P. Korstanje)
    - Use `io.cucumber.junit.CucumberOptions` or `io.cucumber.testng.CucumberOptions` instead
 * [Core] Deprecate `cucumber.api.cli.Main` ([#1670](https://github.com/cucumber/cucumber-jvm/pull/1670) M.P. Korstanje)
    - Use `io.cucumber.core.cli.Main` instead
 * [Core] Deprecate `cucumber.api.Scenario`
    - Use `io.cucumber.core.api.Scenario` instead
 * [Java] Deprecate `cucumber.api.java.*`
    - Use `io.cucumber.java.*` instead
 * [Java] Deprecate `cucumber.api.java8.*`
    - Use `io.cucumber.java8.*` instead
 * [JUnit] Deprecate `cucumber.api.junit.Cucumber`
    - Use `io.cucumber.junit.Cucumber` instead.
 * [TestNG] Deprecate `cucumber.api.testng.TestNGCucumberRunner`
    - Use `io.cucumber.testng.TestNGCucumberRunner` instead.
 * [TestNG] Deprecate `cucumber.api.testng.AbstractTestNGCucumberTests`
    - Use `io.cucumber.testng.AbstractTestNGCucumberTests` instead.
 * [Needle] Deprecate `cucumber.api.needle.*`
    - Use `io.cucumber.needle.*` instead.
 * [Spring] Deprecate `cucumber.api.spring.SpringTransactionHooks`
    - It is recommended to implement your own transaction hooks.
    - Will allow the dependency on `spring-txn` to be removed.

### Note
Use the snapshot version of the cucumber-eclipse plugin for cucumber 4.5.0 and
above that supports the new package structure. To use the latest snapshot
version, refer to [Follow the latest snapshot](https://github.com/cucumber/cucumber-eclipse#follow-the-latest-snapshot)


## [4.4.0] (2019-06-15)

### Added
 * [Core] Add StepDefinedEvent ([#1634](https://github.com/cucumber/cucumber-jvm/pull/1634) Tim te Beek, M.P. Korstanje)
 * [Core] Add CDI2 integration ([#1626](https://github.com/cucumber/cucumber-jvm/pull/1626) Romain Manni-Bucau)
 * [Java] Use ServiceLoader for ObjectFactory ([#1615](https://github.com/cucumber/cucumber-jvm/pull/1615) Toepi, M.P. Korstanje)
   - Object factories that implement `io.cucumber.core.backend.ObjectFactory` will be loaded via the ServiceLoader
 * [Core] Add UnusedStepsSummaryPrinter ([#1648](https://github.com/cucumber/cucumber-jvm/pull/1648) Tim te Beek)
   - Adds `--plugin unused` CLI option
 * [Core] Add reverse and random scenario execution order ([#1645](https://github.com/cucumber/cucumber-jvm/pull/1645), [#1658](https://github.com/cucumber/cucumber-jvm/pull/1658) Grasshopper, M.P. Korstanje)
   - Adds `--order reverse` CLI option
   - Adds `--order random` and `--order random:<seed>` CLI options
   - Adds `--limit <number of pickles>` CLI option

### Changed
 * [Core] Refactored usage formatter ([#1608](https://github.com/cucumber/cucumber-jvm/pull/1608) Marit van Dijk, M.P. Korstanje)
 * [Core] Merge cucumber-html into cucumber-core ([#1650](https://github.com/cucumber/cucumber-jvm/pull/1650) Grasshopper)
 * [Core] Upgrade `cucumber-expressions` version to 7.0.2
   - Support Boolean in BuiltInParameterTransformer ([cucumber/#604](https://github.com/cucumber/cucumber/pull/604) Tommy Wo)
 * [Core] Upgrade `dattable` version to 1.1.14
   - Empty cell are converted to `null`'s for `Double` class ([cucumber/#1617](https://github.com/cucumber/cucumber/pull/1617) Georgii Kalnytskyi)

### Deprecated
 * [Core] Deprecated `StepDefinitionReporter` ([#1634](https://github.com/cucumber/cucumber-jvm/pull/1634) Tim te Beek, M.P. Korstanje)
 * [Java] Deprecated classpath scanning for ObjectFactory ([#1615](https://github.com/cucumber/cucumber-jvm/pull/1615) Toepi, M.P. Korstanje)
    - Deprecated `cucumber.api.java.ObjectFactory` in favour of `io.cucumber.core.backend.ObjectFactory`.

### Fixed
 * [Core] Clear RuntimeOptions.featurePaths if rerun was used ([#1631](https://github.com/cucumber/cucumber-jvm/pull/1631) Tommy Wo)
 * [Core] Escape spaces in ZipResource path ([#1636](https://github.com/cucumber/cucumber-jvm/pull/1636) Bearded QA)
 * [Core] Handle parallel execution exceptions ([#1629](https://github.com/cucumber/cucumber-jvm/pull/1623) Christoph Kutzinski, M.P. Korstanje)
 * [Core] Use meaningful thread names ([#1623](https://github.com/cucumber/cucumber-jvm/pull/1623) Christoph Kutzinski)
 * [Core] Parse UTF-8-BOM feature file ([#1654](https://github.com/cucumber/cucumber-jvm/pull/1654) Grasshopper)
 * [Core] Allow runner to register bus as concurrent or serial event source ([#1656](https://github.com/cucumber/cucumber-jvm/pull/1656) Tim te Beek, M.P. Korstanje)
   - When using JUnit or the CLI the pretty formatter will print steps as the test progresses
 * [Core] Efficiently write JSON to output ([#1663](https://github.com/cucumber/cucumber-jvm/pull/1663) M.P. Korstanje)

## [4.3.1] (2019-05-05)

### Fixed
 * [Core] Fix filtering scenarios loaded from jar ([#1618](https://github.com/cucumber/cucumber-jvm/pull/1618) Denys Zhuravel)

## [4.3.0] (2019-04-11)

### Added
 * [Core] Improve CucumberOptions documentation ([#1573](https://github.com/cucumber/cucumber-jvm/pull/1573) M.P. Korstanje, Marit van Dijk)
 * [Core] Add logger ([#1577](https://github.com/cucumber/cucumber-jvm/pull/1577) M.P. Korstanje)
     - Errors and warnings are now logged via `java.util.Logging` system
     - Glue and feature path config is logged via `java.util.Logging` system
 * [Core] Add real world timestamp to events ([#1591](https://github.com/cucumber/cucumber-jvm/pull/1591) [#1594](https://github.com/cucumber/cucumber-jvm/pull/1594) Yatharth Zutshi, M.P. Korstanje)
     - Adds `start_timestamp` to json formatter output. Timestamp is in ISO8601 format.
     - Fixes the `timeline` plugin visualizing tests as starting in 1970.

### Changed
 * [Core] Simplify duplicate feature detection ([#1602](https://github.com/cucumber/cucumber-jvm/pull/1602) M.P. Korstanje)
 * [Spring] Remove split package ([#1603](https://github.com/cucumber/cucumber-jvm/pull/1603) M.P. Korstanje)
 * [Core] Upgrade cucumber-expressions to v6.2.2
    * Limit explosion of generated expressions to 256 ([#cucumber/576](https://github.com/cucumber/cucumber/pull/576) M.P. Korstanje)
    * Allow parameter-types in escaped optional groups ([#cucumber/572](https://github.com/cucumber/cucumber/pull/572), [#cucumber/561](https://github.com/cucumber/cucumber/pull/561) Luke Hill, Jayson Smith, M.P. Korstanje)
    * Prefer expression with the longest non-empty match ([#cucumber/580](https://github.com/cucumber/cucumber/pull/580) M.P. Korstanje)
    * Improve heuristics for creating Cucumber/Regular Expressions from strings ([#cucumber/518](https://github.com/cucumber/cucumber/pull/518) Aslak Hellesøy)
 * [Kotlin-Java8] Upgrade Kotlin to v1.3.0 and more idiomatic Kotlin ([#1590](https://github.com/cucumber/cucumber-jvm/pull/1590) Marit van Dijk)

### Fixed
 * [Core] Add more details to ParserException ([#1600](https://github.com/cucumber/cucumber-jvm/pull/1600) Yatharth Zutshi)
 * [JUnit] Invoke `@BeforeClass` before `TestRunStarted` event ([#1578](https://github.com/cucumber/cucumber-jvm/pull/1578) M.P. Korstanje)

## [4.2.6] (2019-03-06)

### Fixed
 * [Core] Fix concurrent access issues in JUnit and TestNG formatters ([#1576](https://github.com/cucumber/cucumber-jvm/pull/1576), [#1575](https://github.com/cucumber/cucumber-jvm/issues/1575) M.P. Korstanje, grasshopper7)

## [4.2.5] (2019-03-04)

### Fixed
 * [Core] Fix illegal argument exception when using root package as glue ([#1572](https://github.com/cucumber/cucumber-jvm/pull/1572) M.P. Korstanje)
    * Correctly parses glue path `""` as the root package
    * Correctly parses glue path `"classpath:"` as the root package
    * Correctly parses feature identifier `"classpath:"` as the root package

## [4.2.4] (2019-02-28)

### Fixed
 * [Core] Disambiguate between Windows drive letter and uri scheme ([#1568](https://github.com/cucumber/cucumber-jvm/issues/1568), [#1564](https://github.com/cucumber/cucumber-jvm/issues/1564) jsa34)

## [4.2.3] (2019-02-08)

### Fixed
 * [Build] Fix windows build ([#1552](https://github.com/cucumber/cucumber-jvm/pull/1552), [#1551](https://github.com/cucumber/cucumber-jvm/issues/1551) Alexey Mozhenin)
 * [Core] Formalize glue and feature paths ([#1544](https://github.com/cucumber/cucumber-jvm/pull/1544) M.P. Korstanje)
     * Fixes Line filtering  on Windows ([#1547](https://github.com/cucumber/cucumber-jvm/issues/1547) grasshopper7)
     * Invalid glue and feature paths are no longer silently ignored
     * Explicit references to non-existing feature files are no longer silently ignored

## [4.2.2](https://github.com/cucumber/cucumber-jvm/compare/v4.2.1...v4.2.2)

### Fixed
 * [Core] Fix class loading on Windows ([#1541](https://github.com/cucumber/cucumber-jvm/pull/1529) M.P. Korstanje)
   * Resolves inability to discover glue classes

## [4.2.1](https://github.com/cucumber/cucumber-jvm/compare/v4.2.0...v4.2.1)

### Added
 * [TestNG] Update documentation for parallel execution ([#1501](https://github.com/cucumber/cucumber-jvm/issues/1486) Abhishek Singh)

### Changed
 *  [Core] Parse rerun file in RuntimeOptions ([#1529](https://github.com/cucumber/cucumber-jvm/pull/1529) M.P. Korstanje)

### Fixed
 * [Core] Cache all matched step definitions ([#1528](https://github.com/cucumber/cucumber-jvm/pull/1528) Łukasz Suski)
   * Significant speed up in matching steps to glue on Android
 * [Build] No longer fails to build on OpenJDK 9 and 10. ([#1311](https://github.com/cucumber/cucumber-jvm/issues/1311) M.P. Korstanje)
 * [Core] Error when an explicitly provided feature file does not exist. ([#1529](https://github.com/cucumber/cucumber-jvm/pull/1529) M.P. Korstanje)
    * `path/to/exisitng.feature` will pass
    * `path/to/non/exisitng.feature` will error
    * `path/to/empty/direcory` will pass
  * [Core] Upgrade shaded jackson-databind to 2.9.8 (M.P. Korstanje)
      * Fixes CVE-2018-19360
      * Fixes CVE-2018-14719
      * Fixes CVE-2018-14718
      * Fixes CVE-2018-14721
      * Fixes CVE-2018-14720
      * Fixes CVE-2018-19361
      * Fixes CVE-2018-19362

## [4.2.0] - [Release Announcement](release-notes/v4.2.0.md)

### Added
 * [Core] Add anonymous parameter types ([#1478](https://github.com/cucumber/cucumber-jvm/issues/1478), [#1492](https://github.com/cucumber/cucumber-jvm/pull/1492) M.P. Korstanje)

## [4.1.1](https://github.com/cucumber/cucumber-jvm/compare/v4.1.0...v4.1.1)

### Fixed
 * [Core] Upgrade datatables to 1.1.7 ([#1489](https://github.com/cucumber/cucumber-jvm/issues/1489), [#1490](https://github.com/cucumber/cucumber-jvm/issues/1490) M.P. Korstanje)
    * Fix priority of default converters

## [4.1.0]

### Deprecated
  * [Guice] Replace CucumberModules.SCENARIO with thread safe factory method ([#1486](https://github.com/cucumber/cucumber-jvm/issues/1486) James Bennett)

### Fixed
  * [Core] Use Locale.ROOT when transforming case of identifiers ([#1484](https://github.com/cucumber/cucumber-jvm/issues/1484) M.P. Korstanje)

## [4.0.2]

### Fixed
 * [Core] Fix concurrent execution problems in pretty formatter ([#1480](https://github.com/cucumber/cucumber-jvm/issues/1480) M.P. Korstanje)
 * [Core] Upgrade `cucumber-expressions` 6.1.1.  ([cucumber/#494](https://github.com/cucumber/cucumber/issues/494) Łukasz Suski)
 * [Java8] Apply identity transform to argument when target type is object ([#1477](https://github.com/cucumber/cucumber-jvm/pull/1477) M.P. Korstanje)

## [4.0.1]

### Added
 * [Core] Reduce plugin memory usage ([#1469](https://github.com/cucumber/cucumber-jvm/pull/1469) M.P. Korstanje)

### Changed
 * [Core] Use the docstring content type from pickle in the json formatter ([#1265](https://github.com/cucumber/cucumber-jvm/pull/1265) Robert Wittams, M.P. Korstanje)

### Fixed
 * [Java8] Apply identity transform when target type is unknown ([#1475](https://github.com/cucumber/cucumber-jvm/pull/1475) Daryl Piffre, M.P. Korstanje)

## [4.0.0] - [Release Announcement](release-notes/v4.0.0.md)

### Added
 * [Core] Added extraGlue option to `@CucumberOptions` ([#1439](https://github.com/cucumber/cucumber-jvm/pull/1439) Eduardo Kalinowski)
 * [Core] Support parallel execution of pickles ([#1389](https://github.com/cucumber/cucumber-jvm/pull/1389) Kiel Boatman, M.P. Korstanje)
    * When running with parallel support enabled all Plugins implementing `EventHandler`/`Formater` will receive events
    after execution has completed in `Event.CANONICAL_ORDER`.
    * Plugins implementations implementing `ConcurrentEventListener` will receive events in real time.
    * Plugins implementations are synchronized on and will not receive concurrent events.
    * Added the `--threads`  commandline argument for the CLI.
    * When `--threads` is used with a value greater then 1 parallel support is enabled for the CLI.
    * JUnit/TestNG have parallel support enabled by default. Consult their respective documentation for parallel executions.
 * [Spring] Add documentation for spring object factory ([#1405](https://github.com/cucumber/cucumber-jvm/pull/1405) Marit van Dijk)
 * [Core] Add --wip option ([#1381](https://github.com/cucumber/cucumber-jvm/pull/1381) Heziode)
 * [Core] Upgrade gherkin to 5.1.0  ([#1377](https://github.com/cucumber/cucumber-jvm/pull/1377) Aslak Hellesøy)
 * [Weld] Document the need for a beans.xml per source root ([#923](https://github.com/cucumber/cucumber-jvm/pull/923) Harald Albers)
 * [Core] Upgrade cucumber-expressions to [6.1.0](https://github.com/cucumber/cucumber/blob/master/cucumber-expressions/CHANGELOG.md#610---2018-09-23) ([#1464](https://github.com/cucumber/cucumber-jvm/pull/1464) M.P. Korstanje)
    * ParameterType.fromEnum(MyEnumClass.class) to make it easier to register enums.

### Changed
  * [Core] Upgrade datatable to 1.1.3 ([#1414](https://github.com/cucumber/cucumber-jvm/pull/1414) Łukasz Suski)
    * Allows the registration of default TableEntryByTypeTransformer and TableCellByTypeTransformer
    * Adds DataTableType#entry(Class) to easily map tables to List<SomeClass>.
    * Adds DataTableType#cell(Class) to easily map cells to SomeOtherClass.
  * [Core] Upgrade cucumber expressions to 6.0.0 ([#1377](https://github.com/cucumber/cucumber-jvm/pull/1377) Aslak Hellesøy)
    * Throw an error if a parameter type is used inside optional text parenthesis, or with alternative text.
    * Bugfix for nested capture groups.
  * [Core] Refactor Runtime ([#1367](https://github.com/cucumber/cucumber-jvm/pull/1367) M.P. Korstanje, Marit van Dijk)
    * Significant structural changes in the `cucumber.runtime` package
  * [Examples] Simplify Gradle example ([#1394](https://github.com/cucumber/cucumber-jvm/pull/1394) Piotr Kubowicz)
  * [Build] Use 1.7 as the source and target level for compilation ([#1147](https://github.com/cucumber/cucumber-jvm/pull/1147) M.P. Korstanje)
  * [Core] Use fully classified name for PendingException ([#1398](https://github.com/cucumber/cucumber-jvm/pull/1398) Marit van Dijk)
  * [Core] Update DataTable hint ([#1397](https://github.com/cucumber/cucumber-jvm/pull/1397) Marit van Dijk)

### Deprecated
  * [Core] Deprecate Formatter interface ([#1407](https://github.com/cucumber/cucumber-jvm/pull/1407) Marit van Dijk)

### Removed
  * [Android] Remove Cucumber-Android and move to separate project (Aslak Hellesøy)
  * [TestNG] Remove TestNGReporter ([#1408](https://github.com/cucumber/cucumber-jvm/pull/1408) M.P. Korstanje)
  * [OSGi] Jars are no longer packaged as OSGi bundles. The `osgi` module and `pax-exam` examples have been removed as well.
    ([#1404](https://github.com/cucumber/cucumber-jvm/pull/1404)
     [cucumber/cucumber#412](https://github.com/cucumber/cucumber/issues/412)
     Aslak Hellesøy)
  * [Core] Remove deprecated TestStep methods ([#1391](https://github.com/cucumber/cucumber-jvm/pull/1391) M.P. Korstanje)

### Fixed
  * [Core] Support quoted strings in cucumber.options ([#1453](https://github.com/cucumber/cucumber-jvm/pull/1453) John Patrick)
  * [Core] Set scenario result as step finishes ([#1430](https://github.com/cucumber/cucumber-jvm/pull/1430) M.P. Korstanje)

<!-- Releases -->
[4.8.1]:      https://github.com/cucumber/cucumber-jvm/compare/v4.8.0...v4.8.1
[4.8.0]:      https://github.com/cucumber/cucumber-jvm/compare/v4.7.4...v4.8.0
[4.7.4]:      https://github.com/cucumber/cucumber-jvm/compare/v4.7.3...v4.7.4
[4.7.3]:      https://github.com/cucumber/cucumber-jvm/compare/v4.7.2...v4.7.3
[4.7.2]:      https://github.com/cucumber/cucumber-jvm/compare/v4.7.1...v4.7.2
[4.7.1]:      https://github.com/cucumber/cucumber-jvm/compare/v4.7.0...v4.7.1
[4.7.0]:      https://github.com/cucumber/cucumber-jvm/compare/v4.6.0...v4.7.0
[4.6.0]:      https://github.com/cucumber/cucumber-jvm/compare/v4.5.4...v4.6.0
[4.5.4]:      https://github.com/cucumber/cucumber-jvm/compare/v4.5.3...v4.5.4
[4.5.3]:      https://github.com/cucumber/cucumber-jvm/compare/v4.5.2...v4.5.3
[4.5.2]:      https://github.com/cucumber/cucumber-jvm/compare/v4.5.1...v4.5.2
[4.5.1]:      https://github.com/cucumber/cucumber-jvm/compare/v4.5.0...v4.5.1
[4.5.0]:      https://github.com/cucumber/cucumber-jvm/compare/v4.4.0...v4.5.0
[4.4.0]:      https://github.com/cucumber/cucumber-jvm/compare/v4.3.1...v4.4.0
[4.3.1]:      https://github.com/cucumber/cucumber-jvm/compare/v4.3.0...v4.3.1
[4.3.0]:      https://github.com/cucumber/cucumber-jvm/compare/v4.2.6...v4.3.0
[4.2.6]:      https://github.com/cucumber/cucumber-jvm/compare/v4.2.5...v4.2.6
[4.2.5]:      https://github.com/cucumber/cucumber-jvm/compare/v4.2.4...v4.2.5
[4.2.4]:      https://github.com/cucumber/cucumber-jvm/compare/v4.2.3...v4.2.4
[4.2.3]:      https://github.com/cucumber/cucumber-jvm/compare/v4.2.2...v4.2.3
[4.2.2]:      https://github.com/cucumber/cucumber-jvm/compare/v4.2.1...v4.2.2
[4.2.1]:      https://github.com/cucumber/cucumber-jvm/compare/v4.2.0...v4.2.1
[4.2.0]:      https://github.com/cucumber/cucumber-jvm/compare/v4.1.1...v4.2.0
[4.1.1]:      https://github.com/cucumber/cucumber-jvm/compare/v4.1.0...v4.1.1
[4.1.0]:      https://github.com/cucumber/cucumber-jvm/compare/v4.0.2...v4.1.0
[4.0.2]:      https://github.com/cucumber/cucumber-jvm/compare/v4.0.1...v4.0.2
[4.0.1]:      https://github.com/cucumber/cucumber-jvm/compare/v4.0.0...v4.0.1
[4.0.0]:      https://github.com/cucumber/cucumber-jvm/compare/v3.0.2...v4.0.0
