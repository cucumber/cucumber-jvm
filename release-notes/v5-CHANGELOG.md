# Changelog
This file documents all notable changes for v5.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

----

## [5.7.0] (2020-05-01)

### Added
 * [Java] `Scenario.log(String)` & `Scenario.attach(byte[], String, String)` ([#1893](https://github.com/cucumber/cucumber-jvm/issues/1893) Tim te Beek)
 * [JUnit Platform] Map tags to exclusive resources ([#1944](https://github.com/cucumber/cucumber-jvm/issues/1944) M.P. Korstanje)

### Deprecated
 * [Java] `Scenario.write(String)` & `Scenario.embed(byte[], String, String)` ([#1893](https://github.com/cucumber/cucumber-jvm/issues/1893) Tim te Beek)
 * [Spring] Deprecated `cucumber.xml` and implicit context configuration ([#1940](https://github.com/cucumber/cucumber-jvm/issues/1940) M.P. Korstanje)
   - See [spring/README.md](https://github.com/cucumber/cucumber-jvm/tree/main/spring) for more information
 * [Core] Deprecate multiple tag arguments in @CucumberOptions ([#1955](https://github.com/cucumber/cucumber-jvm/issues/1955) M.P. Korstanje)
   - use a tag expresion instead `@CucumberOptions(tags="(@cucumber or @pickle) and not @salad")`

## [5.6.0] (2020-04-02)

### Added
 * [Spring] Add `@CucumberContextConfiguration` annotation ([#1911](https://github.com/cucumber/cucumber-jvm/issues/1911) Anton Deriabin)
    - Allows `cucumber-spring` to discover application context configurations
      without requiring step definitions or hooks to be available in the class.

### Changed
 * [JUnit Platform] Update `junit-platform` dependency to 1.6.1 ([#1923](https://github.com/cucumber/cucumber-jvm/issues/1923) M.P. Korstanje)
 * [Spring] Update `spring-*` dependency to 5.2.4.RELEASE ([#1923](https://github.com/cucumber/cucumber-jvm/issues/1923) M.P. Korstanje)

### Fixed
 * [Core] Upgrade datatable to v3.3.1 ([#1928](https://github.com/cucumber/cucumber-jvm/issues/1928), [cucumber/cucumber#540](https://github.com/cucumber/cucumber/pull/540) M.P. Korstanje)
    - Correctly throw an exception when converting a horizontal table to a list of Strings
 * [Core] Deduplicate suggest snippets ([#1930](https://github.com/cucumber/cucumber-jvm/issues/1930) M.P. Korstanje)

## [5.5.0] (2020-03-12)

### Added
 * [Java] Add `@ParameterType(useRegexpMatchAsStrongTypeHint=true/false)` ([#1905](https://github.com/cucumber/cucumber-jvm/pull/1914) M.P. Korstanje)

### Fixed
 * [TestNG] Fix concurrent modification of events ([#1919](https://github.com/cucumber/cucumber-jvm/pull/1919) M.P. Korstanje)
   * Resolves an NPE when running TestNG with `parallel="methods"`

## [5.4.2] (2020-03-05)

### Fixed
 * [Gherkin] Fix NPE on empty table ([#1912](https://github.com/cucumber/cucumber-jvm/issues/1912), [#1913](https://github.com/cucumber/cucumber-jvm/pull/1913) M.P. Korstanje)
 * [Java8] update to net.jodah:typetools:0.6.2 ([#1908](https://github.com/cucumber/cucumber-jvm/pull/1908) John Patrick)
    * Adds an automatic modulename to `type-tools`.
 * [Spring] Do not reuse generic application context between scenarios ([#1905](https://github.com/cucumber/cucumber-jvm/pull/1905) M.P. Korstanje)

## [5.4.1] (2020-02-27)

### Fixed
 * [Java8] Add error when types could not be resolved on Java 12+ ([#1902](https://github.com/cucumber/cucumber-jvm/pull/1902) M.P. Korstanje)
 * [All] Remove Jackson from `cucumber-jvm` parent pom ([#1900](https://github.com/cucumber/cucumber-jvm/pull/1900) M.P. Korstanje)

## [5.4.0] (2020-02-20)

### Added
 * [JUnit Platform] Support skipping scenarios with `cucumber.filter.tags` ([#1899](https://github.com/cucumber/cucumber-jvm/pull/1899) M.P. Korstanje)

### Fixed
 * [JUnit Platform] Sort discovered features (M.P. Korstanje)
 * [All] Fix typo in snippet generator message ([#1894](https://github.com/cucumber/cucumber-jvm/pull/1894) Nat Ritmeyer)

## [5.3.0] (2020-02-13)

### Added
 * [Core] CLI should search classpath root by default ([#1889](https://github.com/cucumber/cucumber-jvm/pull/1889) M.P. Korstanje)
 * [Core] Improve error message when incompatible plugins are used
    * `io.qameta.allure.cucumber4jvm.AllureCucumber4Jvm` is not compatible with
       Cucumber resulting in rather hard to explain `NoClassDefFoundError`s.

### Fixed
 * [Core] TestCaseState should be PASSED by default ([#1888](https://github.com/cucumber/cucumber-jvm/pull/1888) M.P. Korstanje)
    * As a result  `Scenario.getState` will return `PASSED` rather then
      `UNDEFINED` prior to the execution of the first step of a scenario.
 * [Core] Rerun formatter returns `/` rather then `.` ([#1892](https://github.com/cucumber/cucumber-jvm/issues/1892) M.P. Korstanje)

## [5.2.0] (2020-02-06)

### Added
 * [Core] Allow Object and String data table types to be redefined ([#1884](https://github.com/cucumber/cucumber-jvm/pull/1884),[#cucumber/885](https://github.com/cucumber/cucumber/pull/885) M.P. Korstanje)

### Fixed
 * [Core] Fix NPE while when failing to invoke a step definition (M.P. Korstanje)
 * [TestNG] Fix NPE in empty scenario ([#1885](https://github.com/cucumber/cucumber-jvm/pull/1885),[#1887](https://github.com/cucumber/cucumber-jvm/pull/1887) M.P. Korstanje)

## [5.1.3] (2020-01-31)

### Fixed
 * [Core] Fix UnknownFormatConversionException in TeamCityPlugin  ([#1881](https://github.com/cucumber/cucumber-jvm/pull/1881) M.P. Korstanje)
 * [Core] Fix NPE when parsing empty feature file ([#1882](https://github.com/cucumber/cucumber-jvm/pull/1882) M.P. Korstanje)

## [5.1.2] (2020-01-27)

### Fixed
 * [JUnit Platform] Build JUnit Platform Engine at Source level 8. (M.P. Korstanje)
   - Entire project was build at source level 9 rather then only `module-info.java`
 * [JUnit Platform] Require `io.cucumber.core.gherkin` as a module dependency. (M.P. Korstanje)

## [5.1.1] (2020-01-26)

### Fixed
 * [Core] Print root cause of exceptions thrown in datatable, parameter and docstring definitions ([#1873](https://github.com/cucumber/cucumber-jvm/pull/1873) M.P. Korstanje)
 * [Core] Escape spaces in resource name ([#1874](https://github.com/cucumber/cucumber-jvm/pull/1874) M.P. Korstanje)

## [5.1.0] (2020-01-25)

### Added
 * [Core] Document supported properties ([#1859](https://github.com/cucumber/cucumber-jvm/pull/1859) M.P. Korstanje)
   - Adds `lexical` value (the default) for `cucumber.execution.order`
 * [TestNG] Run scenarios in customized groups ([#1863](https://github.com/cucumber/cucumber-jvm/pull/1863) Konrad Maciaszczyk, M.P. Korstanje)

### Changed
 * [JUnit Platform] Upgrade JUnit Platform from v1.5.2 to v1.6.0
 * [JUnit Platform] Add module-info for `cucumber-junit-platform-engine` ([#1867](https://github.com/cucumber/cucumber-jvm/pull/1867) M.P. Korstanje, John Patrick)

### Fixed
 * [Java/Java8] Fix NPE in AbstractDatatableElementTransformerDefinition ([#1865](https://github.com/cucumber/cucumber-jvm/pull/1865) Florin Slevoaca, M.P. Korstanje)
 * [Core] Fix collision when using `Datatable.asMaps` ([cucumber/cucumber#877](https://github.com/cucumber/cucumber/pull/877) M.P. Korstanje)
 * [Core] Replace windows path separator in sub package name ([#1869](https://github.com/cucumber/cucumber-jvm/pull/1869) M.P. Korstanje)

## [5.0.0] (2020-01-16) - [Release Notes](release-notes/v5.0.0.md)

### Added
 * [Java] Support empty strings and null values in data tables ([#1857](https://github.com/cucumber/cucumber-jvm/issues/1857) M.P. Korstanje)
   - When registering a table converter `convertToEmptyString` can be used to
   replace a specific value in a datatable (e.g. `[blank]`) with the empty
   string.

### Changed
 * [JUnit] Use JUnit 4.13 in `cucumber-junit` ([#1851](https://github.com/cucumber/cucumber-jvm/issues/1851) John Patrick)
 * [TestNG] Use TestNG 7.1.0 (M.P. Korstanje)

### Fixed
 * [Core] Fixed Illegal character error on Windows ([#1849](https://github.com/cucumber/cucumber-jvm/issues/1849) M.P. Korstanje)
 * [JUnit Platform] Annotate `@Cucumber` with `@Testable` to facilitate discovery by IDEs (M.P. Korstanje)

## [5.0.0-RC4] (2019-12-21)

### Changed
 * [Spring] Share application context ([#1848](https://github.com/cucumber/cucumber-jvm/issues/1848), [#1582](https://github.com/cucumber/cucumber-jvm/issues/1582) Dominic Adatia, Marc Hauptmann, M.P. Korstanje)
   * Share application context between threads [#1846](https://github.com/cucumber/cucumber-jvm/issues/1846)
   * Share application context between Cucumber and JUnit tests [#1583](https://github.com/cucumber/cucumber-jvm/issues/1583)

### Fixed
 * [Core] Handle undefined steps in TeamCityPlugin (M.P. Korstanje)

## [5.0.0-RC3] (2019-12-19)

### Added
 * [Core] Implement TeamCity output format plugin ([#1842](https://github.com/cucumber/cucumber-jvm/issues/1842) M.P. Korstanje)
 * [Core] Support Gherkin Rule keyword ([##1804](https://github.com/cucumber/cucumber-jvm/issues/#1804), [#1840](https://github.com/cucumber/cucumber-jvm/issues/1840) M.P. Korstanje, Aslak Hellesøy)
   - Opt-in by adding `cucumber-gherkin-messages` dependency
   - Not supported by JSON and HTML formatter.
 * [JUnit Platform] Support `line` query parameter in `UriSelector` ([#1845](https://github.com/cucumber/cucumber-jvm/issues/1845) M.P. Korstanje)
 * [Core] Include default gherkin version in version.properties ([#1847](https://github.com/cucumber/cucumber-jvm/issues/1847) David Goss)

### Changed
 * [Core] Throw exception when multiple object factories are found ([#1832](https://github.com/cucumber/cucumber-jvm/issues/1832) M.P. Korstanje)
 * [Core] Print warning when using --non-strict ([#1835](https://github.com/cucumber/cucumber-jvm/issues/1835) M.P. Korstanje)
 * [Core] Throw exception when multiple object factories are found ([#1832](https://github.com/cucumber/cucumber-jvm/issues/1832) M.P. Korstanje)
 * [JUnit Platform] Do not include @ in TestTags ([#1825](https://github.com/cucumber/cucumber-jvm/issues/1825) M.P. Korstanje)

### Fixed
 * [JUnit Platform] Map `SKIPPED` to `TestAbortedException` (M.P. Korstanje)
 * [JUnit Platform] Send events to configured Plugins (M.P. Korstanje)
 * [JUnit Platform] Fix concurrent modification of event queue (M.P. Korstanje)
 * [JUnit Platform] Mark `Constants` as part of the public API (M.P. Korstanje)
 * [Core] DataTable does not support Kotlin Collection types ([#1838](https://github.com/cucumber/cucumber-jvm/issues/1838) Marit Van Dijk, M.P. Korstanje)
     - DataTable types for `X` and `? extends X` are now considered identical.
 * [Core] Ignore class load error while class scanning ([#1843](https://github.com/cucumber/cucumber-jvm/issues/1843), [#1844](https://github.com/cucumber/cucumber-jvm/issues/1844) M.P. Korstanje)

### Deprecated
 * [Core] Deprecate `cucumber.options` ([#1836](https://github.com/cucumber/cucumber-jvm/issues/1836) M.P. Korstanje)
   * Use individual properties instead
       - cucumber.ansi-colors.disabled
       - cucumber.execution.dry-run
       - cucumber.execution.limit
       - cucumber.execution.order
       - cucumber.execution.strict
       - cucumber.execution.wip
       - cucumber.features
       - cucumber.filter.name
       - cucumber.filter.tags
       - cucumber.glue
       - cucumber.object-factory
       - cucumber.plugin
       - cucumber.snippet-type

## [5.0.0-RC2] (2019-11-22)

### Added
 * [Java8] Add DefaultParameter transformer and friends ([#1812](https://github.com/cucumber/cucumber-jvm/issues/1812) M.P. Korstanje)
   - Add `DefaultParameterTransformer` alternative for `TypeRegistry.setDefaultParameterTransformer`
   - Add `DefaultDataTableEntryTransformer` alternative for `TypeRegistry.setDefaultDataTableEntryTransformer`
   - Add `DefaultDataTableCellTransformer` alternative for `TypeRegistry.setDefaultDataTableCellTransformer`
 * [Core] Limited support for classpath scanning in SpringBoot jars ([#1821](https://github.com/cucumber/cucumber-jvm/pull/1821) M.P. Korstanje)
   - Enables scanning of glue and features in `BOOT-INF/classes`.
 * [JUnit Platform] Implement Cucumber as a Junit Platform Engine ([#1530](https://github.com/cucumber/cucumber-jvm/pull/1530), [#1824](https://github.com/cucumber/cucumber-jvm/pull/1824) M.P. Korstanje)

### Changed
 * [Core] Indent write events in PrettyFormatter ([#1809](https://github.com/cucumber/cucumber-jvm/pull/1809) Alexandre Monterroso)
 * [Core] Include file name in duplicate feature detection ([#1819](https://github.com/cucumber/cucumber-jvm/pull/1819) M.P. Korstanje)
 * [Core] Replace ResourceIterable with standard Java solutions ([#1820](https://github.com/cucumber/cucumber-jvm/pull/1820) M.P. Korstanje)
   - Uses `FileSystem` to open URI's allowing features to be read from any file
     system supported by the JVM.

### Deprecated
 * [Core] Deprecate `TypeRegistryConfigurer` ([#1799](https://github.com/cucumber/cucumber-jvm/pull/1799) Anton Deriabin)
     - Use `@ParameterType` and friends instead when using annotation glue.
     - Use `ParameterType` and friends instead when using lambda glue.

## [5.0.0-RC1] (2019-10-11)

### Added
 * [Core] Upgrade the timeline formatter's jQuery dependency from 3.3.1 to 3.4.1. jQuery 3.3.1 has an [XSS vulnerability](https://www.cvedetails.com/cve/CVE-2019-11358/)
   that wouldn't normally affect the timeline formatter. However, it did prevent some organisations from downloading the cucumber-core jar because nexus would block it.
   ([#1759](https://github.com/cucumber/cucumber-jvm/issues/1759), [#1769](https://github.com/cucumber/cucumber-jvm/pull/1769) Vincent Pretre, Aslak Hellesøy)
 * [Core] Add `object-factory` option to CLI and `@CucumberOptions`. ([#1710](https://github.com/cucumber/cucumber-jvm/pull/1710) Ralph Kar)
 * [Java] Allow parameter types access to the test context ([#851](https://github.com/cucumber/cucumber-jvm/issues/851), [#1458](https://github.com/cucumber/cucumber-jvm/issues/1458) M.P. Korstanje)
   - Add `@ParameterType` alternative for `TypeRegistry.defineParameterType`
   - Add `@DataTableType` alternative for `TypeRegistry.defineDataTableType`
   - Add `@DefaultParameterTransformer` alternative for `TypeRegistry.setDefaultParameterTransformer`
   - Add `@DefaultDataTableEntryTransformer` alternative for `TypeRegistry.setDefaultDataTableEntryTransformer`
     - Converts title case headers to property names ([#1751](https://github.com/cucumber/cucumber-jvm/pull/1751) Anton Deriabin, M.P. Korstanje)
   - Add `@DefaultDataTableCellTransformer` alternative for `TypeRegistry.setDefaultDataTableCellTransformer`
   - Add `@DocStringType` alternative for `TypeRegistry.defineDocStringType`
 * [Java8] Allow parameter types access to the test context ([#1768](https://github.com/cucumber/cucumber-jvm/issues/851) Anton Derabin, Tim te Beek)
   - Add `ParameterType` alternative for `TypeRegistry.defineParameterType`
   - Add `DataTableType` alternative for `TypeRegistry.defineDataTableType`
   - Add `DocStringType` alternative for `TypeRegistry.defineDocStringType`
 * [Java] Support repeatable step definition annotations ([#1341](https://github.com/cucumber/cucumber-jvm/issues/1341), [#1467](https://github.com/cucumber/cucumber-jvm/pull/1467) M.P. Korstanje)
 * [Core] Add name to `EmbedEvent` ([#1698](https://github.com/cucumber/cucumber-jvm/pull/1698) Konrad M.)
 * [TestNG] Print suggested snippets per scenario ([#1743](https://github.com/cucumber/cucumber-jvm/pull/1743) M.P. Korstanje)
 * [JUnit] Print suggested snippets per scenario ([#1740](https://github.com/cucumber/cucumber-jvm/pull/1740) M.P. Korstanje)
 * [DeltaSpike] Add ObjectFactory for Apache DeltaSpike ([#1616](https://github.com/cucumber/cucumber-jvm/pull/1616) Toepi)
   - Supports generic CDI containers including Weld, OpenEJB and OpenWebBeans
 * [Core] Add property based runtime options ([#1675](https://github.com/cucumber/cucumber-jvm/pull/1675), [#1741](https://github.com/cucumber/cucumber-jvm/pull/1741) M.P. Korstanje)
    - cucumber.ansi-colors.disabled
    - cucumber.execution.dry-run
    - cucumber.execution.limit
    - cucumber.execution.order
    - cucumber.execution.strict
    - cucumber.execution.wip
    - cucumber.features
    - cucumber.filter.name
    - cucumber.filter.tags
    - cucumber.glue
    - cucumber.object-factory
    - cucumber.plugin
    - cucumber.snippet-type

### Changed
 * [All] New package structure ([#1445](https://github.com/cucumber/cucumber-jvm/pull/1445), [#1448](https://github.com/cucumber/cucumber-jvm/issues/1448), [#1449](https://github.com/cucumber/cucumber-jvm/pull/1449), [#1760](https://github.com/cucumber/cucumber-jvm/pull/1760) M.P. Korstanje)
   - Adds `Automatic-Module-Name` to each module
   - Roots packages in `io.cucumber.<module>`
   - Use `find . -name '*.java' -exec sed -i 's/import cucumber.api/import io.cucumber/g' {} \; -exec sed -i 's/cucumber.CucumberOptions/cucumber.junit.CucumberOptions/g' {} \;` to adopt 90% of the new package structure
   - Use @API Guardian annotations to mark the public API ([#1536](https://github.com/cucumber/cucumber-jvm/issues/1536) M.P. Korstanje)
   - Limits the transitive use `cucumber-core` for regular users
 * [All] Compile using source and target level 8 ([#1611](https://github.com/cucumber/cucumber-jvm/issues/1611) M.P. Korstanje)
 * [Java8] Remove `cucumber-java8` dependency on `cucumber-java`
   - To use both lambda and annotation based step definitions add a dependency on `cucumber-java` and `cucumber-java8`
 * [Core] Load `Backend` implementations via SPI ([#1450](https://github.com/cucumber/cucumber-jvm/issues/1450), [#1463](https://github.com/cucumber/cucumber-jvm/issues/1463) John Patrick, M.P. Korstanje)
 * [Core] Load `ObjectFactory` via SPI
 * [Core] Share object factory  between all backend implementations
    - [CDI2] No longer depends on `cucumber-java`
    - [Guice] No longer depends on `cucumber-java`
    - [Needle] No longer depends on `cucumber-java`
    - [Pico] No longer depends on `cucumber-java`
    - [Spring] No longer depends on `cucumber-java`
    - [Weld] No longer depends on `cucumber-java`
 * [Core] Use feature file language to parse numbers in the type registry
   - Unless explicitly set using the `TypeRegistryConfigurer`
 * [Core] Use Java Time API in Events ([#1620](https://github.com/cucumber/cucumber-jvm/pull/1620) Yatharth Zutshi)
 * [Core] Upgrade `cucumber-expressions` to 8.0.0
    - Simplify heuristics to distinguish between Cucumber Expressions and Regular Expressions ([#cucumber/515](https://github.com/cucumber/cucumber/issues/515), [#cucumber/581](https://github.com/cucumber/cucumber/pull/581), [#1581](https://github.com/cucumber/cucumber-jvm/issues/1581) M.P.Korstanje)
    - Improve decimal number parsing ([#cucumber/600](https://github.com/cucumber/cucumber/issues/600), [#cucumber/605](https://github.com/cucumber/cucumber/pull/605), [#cucumber/669](https://github.com/cucumber/cucumber/issues/669), [#cucumber/672](https://github.com/cucumber/cucumber/pull/672), [#cucumber/675](https://github.com/cucumber/cucumber/pull/675), [#cucumber/677](https://github.com/cucumber/cucumber/pull/677) Aslak Hellesøy, Vincent Psarga, Luke Hill, M.P. Korstanje )
    - Recognize look around as a non-capturing regex group ([#cucumber/481](https://github.com/cucumber/cucumber/issues/576), [#cucumber/593](https://github.com/cucumber/cucumber/pull/593) Luke Hill)
    - Prefer type from method over type from ParameterType   ([#cucumber/658](https://github.com/cucumber/cucumber/pull/658) [#cucumber/659](https://github.com/cucumber/cucumber/pull/659) M.P. Korstanje)
 * [Core] Upgrade `datatable` to 3.0.0
    - Empty cells are `null` values in `DataTable` ([1617](https://github.com/cucumber/cucumber-jvm/issues/1617) M.P. Korstanje)
    - Improve handling of tables without header ([#cucumber/540](https://github.com/cucumber/cucumber/pull/540) M.P. Korstanje)
    - Remove DataTableType convenience methods ([1643](https://github.com/cucumber/cucumber-jvm/issues/1643) M.P. Korstanje)
    - Changes to value type from `Class<?>` to `Type` and return type to `Object` in `TableEntryByTypeTransformer` and `TableCellByTypeTransformer`
 * [TestNG] Upgrades `testng` to 7.0.0 ([#1743](https://github.com/cucumber/cucumber-jvm/pull/1743) M.P. Korstanje)
 * [Core] Add dedicated DocStringTypeRegistry ([#1705](https://github.com/cucumber/cucumber-jvm/pull/1705), [#1745](https://github.com/cucumber/cucumber-jvm/pull/1745) Anton Deriabin, M.P. Korstanje)
    - DocStrings will no longer be converted by table cell converters
    - Adds dedicated `io.cucumber.docstring.DocString` object to use in step definitions
    - Adds `TypeRegistry.defineDocStringType`
    - Adds `@DocStringType` alternative for `TypeRegistry.defineDocStringType`

### Removed
 - [Core] Remove deprecated tag syntax.
 - [Core] Remove `StepDefinitionReporter` ([#1635](https://github.com/cucumber/cucumber-jvm/issues/1635) M.P. Korstanje, Tim te Beek)
   - Listen `StepDefined` events instead
 * [Core] Remove `timeout` ([#1506](https://github.com/cucumber/cucumber-jvm/issues/1506), [#1694](https://github.com/cucumber/cucumber-jvm/issues/1694) M.P. Korstanje)
   - Prefer using library based solutions
    * [JUnit 5 `Assertions.assertTimeout*`](https://junit.org/junit5/docs/5.0.1/api/org/junit/jupiter/api/Assertions.html#assertTimeout-java.time.Duration-org.junit.jupiter.api.function.Executable-)
    * [Awaitility](https://github.com/awaitility/awaitility)
    * [Guava `TimeLimiter`](https://github.com/google/guava/blob/master/guava/src/com/google/common/util/concurrent/TimeLimiter.java)

### Fixed
 - [Java8] Set default before hook order to the same after hook (1000)
 - [Doc] Fixed various Javadoc issues ([#1586](https://github.com/cucumber/cucumber-jvm/pull/1586) Michiel Leegwater)
 - [Doc] Fixed various Javadoc issues (Marit Van Dijk)
 - [JUnit] Always fire TestStarted/Finished for pickle ([#1765](https://github.com/cucumber/cucumber-jvm/pull/1765), [#1785](https://github.com/cucumber/cucumber-jvm/issues/1785) M.P. Korstanje)

<!-- Releases -->
[5.7.0]:  https://github.com/cucumber/cucumber-jvm/compare/v5.6.0...v5.7.0
[5.6.0]:  https://github.com/cucumber/cucumber-jvm/compare/v5.5.0...v5.6.0
[5.5.0]:  https://github.com/cucumber/cucumber-jvm/compare/v5.4.2...v5.5.0
[5.4.2]:  https://github.com/cucumber/cucumber-jvm/compare/v5.4.1...v5.4.2
[5.4.1]:  https://github.com/cucumber/cucumber-jvm/compare/v5.4.0...v5.4.1
[5.4.0]:  https://github.com/cucumber/cucumber-jvm/compare/v5.3.0...v5.4.0
[5.3.0]:  https://github.com/cucumber/cucumber-jvm/compare/v5.2.0...v5.3.0
[5.2.0]:  https://github.com/cucumber/cucumber-jvm/compare/v5.1.3...v5.2.0
[5.1.3]:  https://github.com/cucumber/cucumber-jvm/compare/v5.1.2...v5.1.3
[5.1.2]:  https://github.com/cucumber/cucumber-jvm/compare/v5.1.1...v5.1.2
[5.1.1]:  https://github.com/cucumber/cucumber-jvm/compare/v5.1.0...v5.1.1
[5.1.0]:  https://github.com/cucumber/cucumber-jvm/compare/v5.0.0...v5.1.0
[5.0.0]:  https://github.com/cucumber/cucumber-jvm/compare/v5.0.0-RC4...v5.0.0
[5.0.0-RC4]:  https://github.com/cucumber/cucumber-jvm/compare/v5.0.0-RC3...v5.0.0-RC4
[5.0.0-RC4]:  https://github.com/cucumber/cucumber-jvm/compare/v5.0.0-RC3...v5.0.0-RC4
[5.0.0-RC3]:  https://github.com/cucumber/cucumber-jvm/compare/v5.0.0-RC2...v5.0.0-RC3
[5.0.0-RC2]:  https://github.com/cucumber/cucumber-jvm/compare/v5.0.0-RC1...v5.0.0-RC2
[5.0.0-RC1]:  https://github.com/cucumber/cucumber-jvm/compare/v4.7.1...v5.0.0-RC1
