# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

----
## [Unreleased] (In Git)

### Added

### Changed

### Deprecated

### Removed

### Fixed

## [6.9.1] (2020-12-14)

### Fixed
 * [JUnit Platform] Warn if feature files could not be found ([#2179](https://github.com/cucumber/cucumber-jvm/issues/2179) M.P. Korstanje)
 * [Core] SummaryPrinter outputs clickable links ([#2184](https://github.com/cucumber/cucumber-jvm/issues/2184) M.P. Korstanje)
 * [Core] Merge duplicate plugin options ([#2190](https://github.com/cucumber/cucumber-jvm/issues/2190) M.P. Korstanje)

## [6.9.0] (2020-11-12)

### Deprecated
 * [Plugin] Deprecate `TestRunFinished(Instant)` ([#2169](https://github.com/cucumber/cucumber-jvm/pull/2169) M.P. Korstanje)

### Fixed
 * [Core] Significantly reduce the size of the html report ([cucumber/#1232](https://github.com/cucumber/cucumber/issues/1232) M.P. Korstanje)
 * [Core] Improve error message when plugin paths collide ([#2168](https://github.com/cucumber/cucumber-jvm/pull/2168) M.P. Korstanje)

## [6.8.2] (2020-10-29)

### Fixed
 * [Core] Handle null values in ci-dict [cucumber/#1228](https://github.com/cucumber/cucumber/issues/1228)

### Security
 * [JUnit] Update dependency junit:junit to v4.13.1. 
   * See the [published security advisory](https://github.com/junit-team/junit4/security/advisories/GHSA-269g-pwp5-87pp) for details. 

## [6.8.1] (2020-10-07)

### Fixed

 * [Core] Do not send headers after following redirection.
   ([#1475-cucumber-ruby](https://github.com/cucumber/cucumber-ruby/pull/1475)
    [#2144](https://github.com/cucumber/cucumber-jvm/pull/2144))
 * [Core] Mention `junit-platform.properties` in `--publish` banner. ([#2117](https://github.com/cucumber/cucumber-jvm/pull/2117) M.P. Korstanje)
 * [Core] `--publish` uses banner provided by server. ([#2117](https://github.com/cucumber/cucumber-jvm/pull/2117) M.P. Korstanje)

## [6.8.0] (2020-09-26)

### Added
 * [Core] Define CLI arguments as static variables instead of hardcoded strings ([#2130](https://github.com/cucumber/cucumber-jvm/pull/2130) Quang Lê)
 * [Core] Support for `Optional<T>` in data tables ([cucumber/#1182](https://github.com/cucumber/cucumber/pull/1182) Anton Derabin)

### Fixed
 * [Core] Validation of `CUCUMBER_PUBLISH_TOKEN` no longer happens inside Cucumber-JVM - only on the server.
   ([#2123](https://github.com/cucumber/cucumber-jvm/pull/2123)
    [#2122](https://github.com/cucumber/cucumber-jvm/issues/2122)
    Aslak Hellesøy)

## [6.7.0] (2020-09-14)

### Added
 * [JUnit Platform] Support discovery selectors with FilePosition ([#2121](https://github.com/cucumber/cucumber-jvm/pull/2121) M.P. Korstanje)

### Changed
 * [JUnit Platform] Update dependency org.junit.platform:junit-platform-engine to v1.7.0

## [6.6.1] (2020-09-08)

### Fixed
 * [Core] CucumberOptions default snippet type should not override properties ([#2107](https://github.com/cucumber/cucumber-jvm/pull/2107) M.P. Korstanje)
 * [Core] Replace parentFile.makeDirs with Files.createDirectories(parentFile) ([#2104](https://github.com/cucumber/cucumber-jvm/pull/2104) M.P. Korstanje)
 * [Core] Separate run, dry-run and skip execution modes ([#2102](https://github.com/cucumber/cucumber-jvm/pull/2109), [#2102](https://github.com/cucumber/cucumber-jvm/pull/2109) M.P. Korstanje)
   * Fixes `--dry-run` not failing on undefined steps

### Security
 * [Core] Update `create-meta` to 2.0.2 to avoid sharing credentials ([#2110](https://github.com/cucumber/cucumber-jvm/pull/2110) vincent-psarga)

## [6.6.0] (2020-08-26)

### Added
 * [Core] Boolean system properties and environment variables (`cucumber.*` and `CUCUMBER_*`)
  are strictly parsed. The values `0`, `false`, `no` are interpreted as `false`.
  The values `1`, `true`, `yes` are interpreted as `true`. All other values will
  throw an exception.
  ([#2095](https://github.com/cucumber/cucumber-jvm/pull/2097)
   [#2097](https://github.com/cucumber/cucumber-jvm/pull/2097)
   Aslak Hellesøy)

### Fixed
 * [Core] Issue a PUT request after a GET request responding with 202 and a Location header ([#2099](https://github.com/cucumber/cucumber-jvm/pull/2099) Aslak Hellesøy)

## [6.5.1] (2020-08-20)

### Fixed
 * [Core] Publish instructions now recommend using `src/test/resources/cucumber.properties`.
 ([#2096](https://github.com/cucumber/cucumber-jvm/pull/2096)
  Aslak Hellesøy)

## [6.5.0] (2020-08-17)

### Added
 * [Core] Reports can now be published directly to https://reports.cucumber.io/
   ([#2070](https://github.com/cucumber/cucumber-jvm/pull/2070)
    Aslak Hellesøy, M.P. Korstanje).
   There are several ways to enable this:
   * `@CucumberOptions(publish = true)`
   * `CUCUMBER_PUBLISH_ENABLED=true` (Environment variable)
   * `-Dcucumber.publish.enabled=true` (System property)

## [6.4.0] (2020-07-31)

### Added
 * [Core] Include SourceReferences in message output ([#2064](https://github.com/cucumber/cucumber-jvm/issues/2064) M.P. Korstanje)
 * [Core] Enable searching and filtering in html report ([cucumber/#1111](https://github.com/cucumber/cucumber/pull/1111) Vincent Psarga)
 * [Core] Include `file_name` in `attachment` message  ([cucumber/#2072](https://github.com/cucumber/cucumber/pull/2072) M.P. Korstanje)

### Fixed
 * [Core] Use Unicode symbols as a parameter boundary in snippets ([cucumber/#1108](https://github.com/cucumber/cucumber/pull/1108) M.P. Korstnaje)

## [6.3.0] (2020-07-24)

### Added
 * [Junit Platform] Support cucumber.filter.name ([#2065](https://github.com/cucumber/cucumber-jvm/issues/2065) M.P. Korstanje)

### Changed
 * [OpenEJB] Compiled at source level 8.

### Deprecated
 * [Weld] Deprecate `cucumber-weld` ([#1763](https://github.com/cucumber/cucumber-jvm/issues/1763) M.P. Korstanje)
    * Consider using cucumber-deltaspike instead
 * [Needle] Deprecate `cucumber-needle` ([#1763](https://github.com/cucumber/cucumber-jvm/issues/1763) M.P. Korstanje)
    * Consider using cucumber-deltaspike instead

### Fixed
 * [Core] Improve error message when an unknown plugin is used ([#2053](https://github.com/cucumber/cucumber-jvm/issues/2053) M.P. Korstanje)
 * [Java8] Allow test execution context to be garbage collected ([#2067](https://github.com/cucumber/cucumber-jvm/issues/2067) M.P. Korstanje)

## [6.2.2] (2020-07-09)

### Fixed
 *  [JUnit] Make duplicate pickle names unique ([#2045](https://github.com/cucumber/cucumber-jvm/issues/2045) M.P. Korstanje)

## [6.2.1] (2020-07-07)

### Fixed
 * [Core] Follow symlinks when loading feature files ([#2043](https://github.com/cucumber/cucumber-jvm/issues/2043) Andrey Mukamolov)

## [6.2.0] (2020-07-02)

### Changed
 * [Core] Upgrade to [Gherkin v14](https://github.com/cucumber/cucumber/blob/master/gherkin/CHANGELOG.md#1400---2020-06-27)

### Fixed
 * [Core] Render attachments in `html` formatter

## [6.1.2] (2020-06-25)

### Fixed
 * [Core] Update `cucumber-expressions` to v10.2.1
  * Retain position of optional groups ([cucumber/#1076](https://github.com/cucumber/cucumber/pull/1076), [cucumber/#511](https://github.com/cucumber/cucumber/pull/511), [cucumber/#952](https://github.com/cucumber/cucumber/pull/952) M.P. Korstanje)
 * [Core] Generate valid parameter names in snippets ([#2029](https://github.com/cucumber/cucumber-jvm/issues/2029) M.P. Korstanje)

## [6.1.1] (2020-06-12)

### Added
 * [JUnit] Warn about usage of io.cucumber.testng.CucumberOptions
 * [TestNG] Warn about usage of io.cucumber.junit.CucumberOptions

### Fixed
 * [Core] Always use UTF8 encoding ([#2021](https://github.com/cucumber/cucumber-jvm/issues/2021) M.P. Korstanje)

## [6.1.0] (2020-06-11)

### Added
 * [CDI Jakarta] Implement ObjectFactory using CDI Jakarta ([#2009](https://github.com/cucumber/cucumber-jvm/issues/2009) Romain Manni-Bucau)
 * [Core] Add location to tag expression exception ([#1979](https://github.com/cucumber/cucumber-jvm/issues/1979) Christopher Yocum)

### Fixed
 * [Core] Correct issue with usage report durations >= 1 second ([#1989](https://github.com/cucumber/cucumber-jvm/issues/1989) Dan Woodward)
 * [Core] Fix locale in DefaultSummaryPrinter ([#2010](https://github.com/cucumber/cucumber-jvm/issues/2010) Romain Manni-Bucau, M.P. Korstanje)

## [6.0.0] (2020-06-07) - [Release Notes](release-notes/v6.0.0.md)

### Added
* [Spring] Add `@ScenarioScope` annotation ([#1974](https://github.com/cucumber/cucumber-jvm/issues/1974) M.P. Korstanje)
  * Preferable to `@Scope(value = SCOPE_CUCUMBER_GLUE)`

### Fixed
 * [Plugin] Restored `Status.isOk(boolean isStrict)` to avoid breaking existing plugins
 * [Core] Execute features files without pickles ([#1973](https://github.com/cucumber/cucumber-jvm/issues/1973) M.P. Korstanje)
 * [Spring] Require an active scenario before creating beans ([#1974](https://github.com/cucumber/cucumber-jvm/issues/1974) M.P. Korstanje)
 * [Core] Fix NPE in `CucumberExecutionContext.emitMeta` when in a shaded jar (M.P. Korstanje)
 * [Core] Fix line filter for scenario outlines ([#1981](https://github.com/cucumber/cucumber-jvm/issues/1981) M.P. Korstanje)
 * [Core] cucumber.feature preserves tags when used with a feature argument  ([#1986](https://github.com/cucumber/cucumber-jvm/issues/1986) M.P. Korstanje)

## [6.0.0-RC2] (2020-05-03)

### Added
 * [Plugin] Add TestSourceParsed event ([#1895](https://github.com/cucumber/cucumber-jvm/issues/1895) M.P. Korstanje)

### Changed
 * [Core] Default to `--strict` mode ([#1960](https://github.com/cucumber/cucumber-jvm/issues/1960) M.P. Korstanje)
 * [Java] Switch `useRegexpMatchAsStrongTypeHint` default to false ([#1915](https://github.com/cucumber/cucumber-jvm/issues/1915) M.P. Korstanje)

### Removed
 * [Core] Remove multiple tag arguments in `@CucumberOptions` ([#1948](https://github.com/cucumber/cucumber-jvm/issues/1948) M.P. Korstanje)
 * [Core] Remove `cucumber.options` property ([#1958](https://github.com/cucumber/cucumber-jvm/issues/1958) M.P. Korstanje)
 * [Spring] Remove cucumber.xml and implied context configuration ([#1959](https://github.com/cucumber/cucumber-jvm/issues/1959) M.P. Korstanje)
 * [Guice] Remove deprecated scenario scope and module constant

### Fixed
 * [Core] Include all fields in `JsonFormatters` failure feature ([#1954](https://github.com/cucumber/cucumber-jvm/issues/1954) M.P. Korstanje)

### Security
 * [Core] Upgrade jQuery to [3.5.1](https://blog.jquery.com/2020/04/10/jquery-3-5-0-released/) ([#1971](https://github.com/cucumber/cucumber-jvm/issues/1971) M.P. Korstanje)

## [6.0.0-RC1] (2020-04-23)

### Added
 *  [Core] Support limited set of cURL commands for UrlOutputStream ([#1910](https://github.com/cucumber/cucumber-jvm/issues/#1910), [#1932](https://github.com/cucumber/cucumber-jvm/issues/1932) M.P. Korstanje, Aslak Hellesøy)

### Changed
 * [Core] Use Gherkin 6+ and Cucumber messages ([#1841](https://github.com/cucumber/cucumber-jvm/issues/1841), [#1941](https://github.com/cucumber/cucumber-jvm/issues/1941), [#1942](https://github.com/cucumber/cucumber-jvm/issues/1942) M.P. Korstanje, Aslak Hellesøy)
   - Implements the `message` formatter which will output cucumber messages as ndjson
   - Implements improved `html`formatter
      - Change your plugin options `html:target/cucumber-html` to `html:target/cucumber-html/index.html`
   - Switches the default parser to Gherkin 6+. This will enable rule support by default.
   - Adds exception to `TestRunFinished` event.
   - Adds error state json to `JsonFormatter` output.
   - Add exception handling around Cucumber execution. When execution fails, Cucumber will still emit the `TestRunFinished` event.

### Fixed
 * [Java] Invoke static methods without instantiating target object ([#1953](https://github.com/cucumber/cucumber-jvm/issues/1953) M.P. Korstanje)

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

## [3.0.2]

### Fixed
* [Android] Fix PatternSyntaxException ([#1370](https://github.com/cucumber/cucumber-jvm/pull/1370) M.P. Korstanje)

## [3.0.1](https://github.com/cucumber/cucumber-jvm/compare/v3.0.0...v3.0.1)

### Changed
 * [Core] Upgrade cucumber expressions to 5.0.18 ([#1368](https://github.com/cucumber/cucumber-jvm/pull/1368) Aslak Hellesøy)
     * Escape `/` with `\/` when a literal `/` is wanted (and not alternation)

### Fixed
 * [Core] Fix the handling of step output in HTML Formatter ([#1349](https://github.com/cucumber/cucumber-jvm/issues/1349) Björn Rasmusson)

## [3.0.0] - [Release Announcement](release-notes/v3.0.0.md)

### Added
 * [Core] Implement [cucumber expressions](https://github.com/cucumber/cucumber/tree/master/cucumber-expressions) ([#1248](https://github.com/cucumber/cucumber-jvm/pull/1248) M.P. Korstanje, Björn Rasmusson, Marit van Dijk, Aslak Hellesøy)
   * Custom parameter types can be defined by implementing the `TypeRegistryConfigurer`.
 * [Core] Add Before and AfterStep hooks ([#1323](https://github.com/cucumber/cucumber-jvm/pull/1323) Aniket, Björn Rasmusson, M.P. Korstanje)
 * [Core, TestNG] Support the TestNG SkipException ([#1338](https://github.com/cucumber/cucumber-jvm/pull/1338), [#1340](https://github.com/cucumber/cucumber-jvm/pull/1340) Björn Rasmusson, M.P. Korstanje)

### Changed
 * [TestNG] Update testng version to 6.14.3
 * [OpenEJB] Update openejb-core version to 4.7.5
 * [Core] Replace DataTable with [io.cucumber.datatable.DataTable](https://github.com/cucumber/cucumber/tree/master/datatable) ([#1248](https://github.com/cucumber/cucumber-jvm/pull/1248) M.P. Korstanje, Björn Rasmusson, Marit van Dijk)
   * Custom data table types can be defined by implementing the `TypeRegistryConfigurer`.
 * [Core] Include all hooks in the event stream generated by `--dry-run` ([#1323](https://github.com/cucumber/cucumber-jvm/pull/1323) Aniket, Björn Rasmusson, M.P. Korstanje)
 * [Spring] Limit context configuration to a single class. ([#1240](https://github.com/cucumber/cucumber-jvm/pull/1240), [#1246](https://github.com/cucumber/cucumber-jvm/pull/1246) Björn Rasmusson, M.P. Korstanje)
 * [Core] The SummaryPrinter only depends on api classes ([#1361](https://github.com/cucumber/cucumber-jvm/pull/1361) Björn Rasmusson, M.P. Korstanje)
     * `print(Runtime runtime)` has been removed from `SummaryPrinter` it can be replaced by implementing `EventListener`

### Deprecated
 * [Core] Deprecate all methods but `TestStep.getCodeLocation` in favour of PickleStepTestStep and HookTestStep ([#1323](https://github.com/cucumber/cucumber-jvm/pull/1323) Aniket, Björn Rasmusson, M.P. Korstanje)

### Removed
 * [Core] Remove UnreportedStepExecutor ([#1362](https://github.com/cucumber/cucumber-jvm/pull/1362) M.P. Korstanje)
 * [Core] Removed XStream and related functionality ([#1248](https://github.com/cucumber/cucumber-jvm/pull/1248) M.P. Korstanje, Björn Rasmusson, Marit van Dijk, Aslak Hellesøy)
   * `@Delimiter`, `@Format`, `@Transformer`,`@XStreamConverter`, `@XStreamConverters` and any other
     annotations from XStream will no longer work. These must be replaced by a `DataTableType` or `ParameterType`.
 * [Core] Remove deprecated constructors of `TestStep` ([#1323](https://github.com/cucumber/cucumber-jvm/pull/1323) Aniket, Björn Rasmusson, M.P. Korstanje)
 * [TestNG] Remove the support of mapping the whole test suite or each feature to TestNG tests ([#1339](https://github.com/cucumber/cucumber-jvm/pull/1339), [#1340](https://github.com/cucumber/cucumber-jvm/pull/1340) Björn Rasmusson, M.P. Korstanje)
 * [JUnit] Remove the obsolete JUnit option `--allow-started-ignored` (Björn Rasmusson)
 * [JUnit] Remove Cucumber.createRuntime method ([#1287](https://github.com/cucumber/cucumber-jvm/pull/1287) M.P. Korstanje)
 * [Core] Remove 'dummy' results from junit formatter ([#1331](https://github.com/cucumber/cucumber-jvm/pull/1331), [#1326](https://github.com/cucumber/cucumber-jvm/pull/1326) Christoph Kutzinski)
 * [Core] Remove the deprecated -f/--format option ([#1295](https://github.com/cucumber/cucumber-jvm/pull/1295) Björn Rasmusson, M.P. Korstanje)

### Fixed
 * [Core] Remove excess hyphens in `--help` usage info ([#1347](https://github.com/cucumber/cucumber-jvm/pull/1347) Jano Svitok
 * [Core] Fix runtime exit status for ambiguous scenarios ([#1342](https://github.com/cucumber/cucumber-jvm/pull/1342) Prashant Ramcharan)

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

## [1.2.6] (2019-11-09)
* [All] Maven distribution relocation ([#1336](https://github.com/cucumber/cucumber-jvm/issues/1336) John Patrick, M.P. Korstanje)
  * After upgrading to 1.2.6 Maven will let users know that the `groupId` has
    changed from `info.cukes` to `io.cucumber`.

## [1.2.5] - [Release Announcement](release-notes/v1.2.5.md)

* [Java8] Fix closing over local variables ([#916](https://github.com/cucumber/cucumber-jvm/issues/916), [#924](https://github.com/cucumber/cucumber-jvm/pull/924), [#929](https://github.com/cucumber/cucumber-jvm/pull/929) Alexander Torstling, Aslak Hellesøy)
* [Java8] Fix IllegalArgumentException on JDK 1.8.0_60 ([#912](https://github.com/cucumber/cucumber-jvm/issues/912), [#914](https://github.com/cucumber/cucumber-jvm/pull/914) Michael Wilkerson)
* [Core] Double-check for directory exists in the ensureParentDirExists(File) ([#978](https://github.com/cucumber/cucumber-jvm/pull/978) Pavel Ordenko)
* [picocontainer] Picocontainer lifecycle support([#994](https://github.com/cucumber/cucumber-jvm/pull/994), [#993](https://github.com/cucumber/cucumber-jvm/issues/993), [#992](https://github.com/cucumber/cucumber-jvm/pull/992) Richard Bradley)
* [Core] Specifying plugins on the command line via `--plugin` clobbers settings in the code ([#860](https://github.com/cucumber/cucumber-jvm/pull/860) Björn Rasmusson)
* [Core] Make test assertion OS agnostic ([#897](https://github.com/cucumber/cucumber-jvm/pull/897/files) sid)
* [Travis] Improve the travis build ([#829](https://github.com/cucumber/cucumber-jvm/pull/829) Björn Rasmusson)
* [Core, Junit] Passthrough options for the JUnit Module ([#1002](https://github.com/cucumber/cucumber-jvm/pull/1002), [#1029](https://github.com/cucumber/cucumber-jvm/pull/1029). Also resolves [#825](https://github.com/cucumber/cucumber-jvm/pull/825) and [#972](https://github.com/cucumber/cucumber-jvm/issues/972). Björn Rasmusson, with bug fix by Bernd Bindreiter)
* [Android, Core, Guice, Junit] Improve code quality ensuring Utility Classes cannot be instantiated ([#945](https://github.com/cucumber/cucumber-jvm/pull/945) Kirill Vlasov)
* [Core, Guice, Testng] Improve code quality using isEmpty instead of size comparison ([#942](https://github.com/cucumber/cucumber-jvm/pull/942) Kirill Vlasov)
* [Clojure] Don't depend on cucumber-core in the clojure example ([#947](https://github.com/cucumber/cucumber-jvm/pull/947) Joe Corneli)
* [Spring] Some spring tests were not being run ([#952](https://github.com/cucumber/cucumber-jvm/pull/952) Lee Wan Geun)
* [OSGI] Pax-exam is optional ([#1000](https://github.com/cucumber/cucumber-jvm/pull/1000) HendrikSP)
* [Groovy] Fix Null Pointer Exeption whe using List as parameter type in step defs ([#980](https://github.com/cucumber/cucumber-jvm/pull/980) Steffen Jacobs)
* [Readme] Use SVG badges! ([#941](https://github.com/cucumber/cucumber-jvm/pull/941) Kevin Goslar)
* [TestNG] Ignore the testng directory ([#990](https://github.com/cucumber/cucumber-jvm/pull/990) Jan Molak)
* [Core] Use Integer.compare() in HookComparator in order to guard against possible underflow ([#986](https://github.com/cucumber/cucumber-jvm/pull/986), [#985](https://github.com/cucumber/cucumber-jvm/issues/985) Mikael Auno)
* [Junit] Let JUnitReporter treat Pending results in hooks as failures in strict mode, and as ignored tests otherwise (Björn Rasmusson)
* [Core] Mark scenario as skipped in JUnitFormatter if PendingException is thrown in a hook ([#964](https://github.com/cucumber/cucumber-jvm/pull/964), [#962](https://github.com/cucumber/cucumber-jvm/issues/962) Felix Martin Martin)
* [Core] Support assume feature also with JUnit 4.12 ([#961](https://github.com/cucumber/cucumber-jvm/pull/961) Stefan Birkner)
* [TestNG] Always tear down TestNG cucumber tests ([#955](https://github.com/cucumber/cucumber-jvm/issues/955), [#956](https://github.com/cucumber/cucumber-jvm/pull/956) Sven-Torben Janus)
* [TestNG] Make TestNG to fail on unparseable feature files ([#953](https://github.com/cucumber/cucumber-jvm/issues/953) Björn Rasmusson)
* [Java8] Throw better exception when lambda stepdefs use generic list arguments (unsupported) (Aslak Hellesøy)

## [1.2.4] (2015-07-23)

* [Core] DocString arguments can be converted to scalar types just like capture group arguments (Aslak Hellesøy)
* [Guice] The `cucumber-guice.properties` file is no longer used. Use `cucumber.properties` instead.
* [Guice] The `guice.injector-source` property can be overridden as a System property or environment variable ([#881](https://github.com/cucumber/cucumber-jvm/issues/881) Aslak Hellesøy)
* [Java] `ObjectFactory.addClass` returns a boolean indicating whether or not stepdefs/hooks for that class should be registered. (Aslak Hellesøy)
* [examples] Fix to allow lein test to to run successfully ([#805](https://github.com/cucumber/cucumber-jvm/pull/805) Chris Howe-Jones)

## [1.2.3] (2015-07-07)

* [Core] Make the Rerun Formatter consistent with the exit code ([#871](https://github.com/cucumber/cucumber-jvm/pull/871) Björn Rasmusson)
* [OSGi] Cucumber is ready to run in OSGi containers ([#873](https://github.com/cucumber/cucumber-jvm/pull/873), [#799](https://github.com/cucumber/cucumber-jvm/pull/799) @HendrikSP)
* [Java] `cucumber.runtime.java.ObjectFactory` moved to `cucumber.api.java.ObjectFactory`. Custom implementation can
  be specified in `cucumber.properties` with `cucumber.api.java.ObjectFactory=my.special.KindOfObjectFactory`. (Closes [#290](https://github.com/cucumber/cucumber-jvm/issues/290) Aslak Hellesøy)
* [Core] Properly decode jar URLs with spaces (%20) - ([#866](https://github.com/cucumber/cucumber-jvm/issues/866) Aslak Hellesøy)
* [Java] Arity mismatch Java8 Step Definition error ([#852](https://github.com/cucumber/cucumber-jvm/issues/852), [#847](https://github.com/cucumber/cucumber-jvm/pull/847) David Coelho)
* [Java] Print Java 8 lambda snippets when `cucumber-java8` is active (Aslak Hellesøy)
* [Core] Make the Summary Printer into a plugin ([#828](https://github.com/cucumber/cucumber-jvm/pull/828) Björn Rasmusson)
* [Core] Additional unit-tests for [#789](https://github.com/cucumber/cucumber-jvm/issues/789) ([#815](https://github.com/cucumber/cucumber-jvm/pull/815) Klaus Bayrhammer)
* [Java] Added @Documented to all step annotations ([#834](https://github.com/cucumber/cucumber-jvm/pull/834), [#833](https://github.com/cucumber/cucumber-jvm/issues/833) Peter Oxenham)
* [Core] Set a description for Scenario Outline scenarios ([#841](https://github.com/cucumber/cucumber-jvm/pull/841), [#837](https://github.com/cucumber/cucumber-jvm/issues/837) Björn Rasmusson)
* [Core] Call all formatters, also in case of unimplemented methods ([#842](https://github.com/cucumber/cucumber-jvm/pull/842), [#803](https://github.com/cucumber/cucumber-jvm/issues/803) Björn Rasmusson)
* [TestNG] Run each feature as separate TestNG test ([#817](https://github.com/cucumber/cucumber-jvm/pull/817), [#653](https://github.com/cucumber/cucumber-jvm/pull/653) Dmitry Sidorenko, Björn Rasmusson)
* [Core] Implement TestNG-compatible XML formatter ([#818](https://github.com/cucumber/cucumber-jvm/pull/818), [#621](https://github.com/cucumber/cucumber-jvm/pull/621) Dmitry Berezhony, Björn Rasmusson)
* [Core] `DataTable.diff(List)` gives proper error message when the `List` argument is empty (Aslak Hellesøy)
* [Core] Execute no scenarios when the rerun file is empty ([#840](https://github.com/cucumber/cucumber-jvm/issues/840) Björn Rasmusson)
* [Core] Snippets for quoted arguments changed from `(.*?)` to `([^\"]*)` (which is how it was before 1.1.6). See [cucumber/cucumber#663](https://github.com/cucumber/cucumber/pull/663) (Aslak Hellesøy)
* [Core] Fix non running gradle example ([#839](https://github.com/cucumber/cucumber-jvm/pull/839) Ole Christian Langfjæran)
* [Clojure] Improved documentation for the clojure module ([#864](https://github.com/cucumber/cucumber-jvm/pull/864) Paul Doran)

## [1.2.2] (2015-01-13)

(There is no 1.2.1 release)

* [Core] Look up `cucumber.options` from `cucumber.properties` (Previously only `CUCUMBER_OPTIONS` was working). (Aslak Hellesøy)
* [Android] put android project into default profile ([#821](https://github.com/cucumber/cucumber-jvm/pull/821) Sebastian Gröbler, Björn Rasmusson)
* [Android] made android studio example use espresso 2 ([#820](https://github.com/cucumber/cucumber-jvm/pull/820) Sebastian Gröbler)
* [Android] removed apklib generation from android project, fixed and cleaned up android examples ([#819](https://github.com/cucumber/cucumber-jvm/pull/819) Sebastian Gröbler, Björn Rasmusson)
* [Groovy] Add support for execution order for Groovy hooks ([#809](https://github.com/cucumber/cucumber-jvm/pull/809), [#807](https://github.com/cucumber/cucumber-jvm/issues/807) Mohammad Shamsi)
* [JUnit] JUnit 4.12 compatibility ([#794](https://github.com/cucumber/cucumber-jvm/pull/794), [#792](https://github.com/cucumber/cucumber-jvm/issues/792) Johann Vanackere)
* [Java] Java 8 lambda step definitions. ([#738](https://github.com/cucumber/cucumber-jvm/issues/738), [#767](https://github.com/cucumber/cucumber-jvm/pull/767) Romain Manni-Bucau, Aslak Hellesøy with help from Dan Bodart).
* [Core] Handles zip/jar protocols ([#808](https://github.com/cucumber/cucumber-jvm/issues/808), Rui Figueira)
* [Core] Handles multiple classloaders ([#814](https://github.com/cucumber/cucumber-jvm/pull/814), Gerard de Leeuw)

## [1.2.0] (2014-10-30)

* [Clojure] Added clojure_cukes example to the maven build ([#790](https://github.com/cucumber/cucumber-jvm/pull/790) Jestine Paul)
* [Spring] Added Spring meta-annotation support ([#791](https://github.com/cucumber/cucumber-jvm/pull/791) Georgios Andrianakis)
* [JUnit] Improve consistency between JUnit and Command Line Runners ([#765](https://github.com/cucumber/cucumber-jvm/pull/765) cliviu)
* [Core] Clobber all filter types when override one filter type in the environment options ([#748](https://github.com/cucumber/cucumber-jvm/pull/748) Björn Rasmusson)
* [Android] Big refactoring ([#766](https://github.com/cucumber/cucumber-jvm/pull/766) Sebastian Gröbler)
* [Android] Improve documentation ([#772](https://github.com/cucumber/cucumber-jvm/pull/772) K76154)
* [Core] New --i18n option for printing keywords ([#785](https://github.com/cucumber/cucumber-jvm/pull/785) Seb Rose)
* [Core] Make the JUnit formatter handle empty scenarios ([#774](https://github.com/cucumber/cucumber-jvm/issues/774) Björn Rasmusson)
* [Scala] Fixing randomly failing tests in the Scala module ([#768](https://github.com/cucumber/cucumber-jvm/pull/768), [#761](https://github.com/cucumber/cucumber-jvm/issues/761) Manuel Bernhardt)
* [JRuby] cucumber-jruby backend fails to build when `RUBY_VERSION` is present in environment ([#718](https://github.com/cucumber/cucumber-jvm/issues/718) Aslak Hellesøy)
* [Core] `DataTable.asMap()` returns a `LinkedHashMap`, ensuring key iteration order is the same as in the gherkin table ([#764](https://github.com/cucumber/cucumber-jvm/issues/764) Aslak Hellesøy).
* [Core] Spring dirty cukes test fix ([#708](https://github.com/cucumber/cucumber-jvm/pull/708) Mykola Gurov)
* [Core] Improve error message for multiple formatters using STDOUT ([#744](https://github.com/cucumber/cucumber-jvm/pull/744) Björn Rasmusson)
* [Core] Better error messages when loading features from rerun file ([#749](https://github.com/cucumber/cucumber-jvm/pull/749) Björn Rasmusson)
* [Core] Handle "" properly in ListConverter. ([#756](https://github.com/cucumber/cucumber-jvm/pull/756) Clément MATHIEU)
* [Guice] Update links and fix formatting in Cucumber Guice docs ([#763](https://github.com/cucumber/cucumber-jvm/pull/763) Jake Collins)
* [Groovy] Clean up groovy stack traces ([#758](https://github.com/cucumber/cucumber-jvm/pull/758) Tom Dunstan)
* [Gosu] New module. (Aslak Hellesøy)
* [Gosu] Modified When Expression to use a void block. (Mark Sayewich)
* [Ioke] Removed this module. It slows down the build and is too esoteric.
* [Core] Richer plugin API. The `--plugin` option can specify a class that implements one or more of `gherkin.formatter.Formatter,gherkin.formatter.Reporter,cucumber.api.StepDefinitionReporter` (Aslak Hellesøy)
* [Core] Removed support for `--dotcucumber` and `stepdefs.json`. The new plugin API replaces this with `cucumber.api.StepDefinitionReporter` (Aslak Hellesøy)
* [Core] The `--format` option is deprecated in favour of `--plugin` (Aslak Hellesøy)
* [JUnit] `@cucumber.junit.api.Cucumber.Options` that was deprecated in 1.1.5 has been removed. Use `@cucumber.api.CucumberOptions` (Aslak Hellesøy)
* [Android] Fix the Android build on Travis ([#750](https://github.com/cucumber/cucumber-jvm/pull/750) Björn Rasmusson)
* [Core] Handle NullPointerExceptions in MethodFormat.getCodeSource ([#757](https://github.com/cucumber/cucumber-jvm/pull/757), [#751](https://github.com/cucumber/cucumber-jvm/pull/751) bySabi)
* [Core] Correct lookup environment variable - system property - resource bundle ([#754](https://github.com/cucumber/cucumber-jvm/pull/754) Björn Rasmusson)
* [Android,Spring,Needle,Examples] Remove commons-logging & log4j and redirect all logging to slf4j & logback ([#742](https://github.com/cucumber/cucumber-jvm/pull/742) Nayan Hajratwala)
* [Spring] Fix the glue class autowiring, transaction and cucumber-glue scope issues ([#711](https://github.com/cucumber/cucumber-jvm/pull/711), [#600](https://github.com/cucumber/cucumber-jvm/issues/600), [#637](https://github.com/cucumber/cucumber-jvm/issues/637) Björn Rasmusson)
* [Groovy] Support more then one `World {}` definition ([#716](https://github.com/cucumber/cucumber-jvm/pull/716) Anton)

## [1.1.8] (2014-06-26)

* [JUnit] Let JUnitReporter fire event(s) on the step notifier for every step ([#656](https://github.com/cucumber/cucumber-jvm/pull/656) Björn Rasmusson)
* [JUnit] Correct JUnit notification for background steps. ([#660](https://github.com/cucumber/cucumber-jvm/pull/660), [#659](https://github.com/cucumber/cucumber-jvm/issues/659) Björn Rasmusson)
* [Core] Expose Scenario id to step definitions ([#673](https://github.com/cucumber/cucumber-jvm/pull/673), [#715](https://github.com/cucumber/cucumber-jvm/issues/715) Björn Rasmusson)
* [Core] The RuntimeOptionsFactory should add default feature path, glue path and formatter once. ([#636](https://github.com/cucumber/cucumber-jvm/pull/636), [#632](https://github.com/cucumber/cucumber-jvm/pull/632), [#633](https://github.com/cucumber/cucumber-jvm/pull/633) Björn Rasmusson)
* [Clojure] Update clojure version to 1.6.0 ([#698](https://github.com/cucumber/cucumber-jvm/pull/698) Jeremy Anderson)
* [Core] Only include executed scenarios and steps from outlines in the JSON output ([#704](https://github.com/cucumber/cucumber-jvm/pull/704) Björn Rasmusson)
* [JUnit] JUnitFormatter: use ascending numbering of outline scenarios ([#706](https://github.com/cucumber/cucumber-jvm/pull/706) Björn Rasmusson)
* [TestNG] Let the TestNG runner handle strict mode correctly ([#719](https://github.com/cucumber/cucumber-jvm/pull/719) Björn Rasmusson)
* [Core] Disregard order of JSON properties in PrettyPrint unit tests ([#740](https://github.com/cucumber/cucumber-jvm/pull/740) mchenryc)
* [Core] Support reading feature paths from the rerun formatter file ([#726](https://github.com/cucumber/cucumber-jvm/pull/726) Björn Rasmusson)
* [Core] Apply line filters only to the feature path that they are defined on ([#725](https://github.com/cucumber/cucumber-jvm/pull/725) Björn Rasmusson)
* [Groovy] Allow tests to run multi-threaded in the same JVM ([#723](https://github.com/cucumber/cucumber-jvm/issues/723), [#727](https://github.com/cucumber/cucumber-jvm/pull/727) Bradley Hart)
* [Core] New `DataTable.unorderedDiff` method ([#731](https://github.com/cucumber/cucumber-jvm/pull/731), [#732](https://github.com/cucumber/cucumber-jvm/issues/732) yoelb)
* [Core] Dynamically constructed converter for class with constructor assignable from String ([#735](https://github.com/cucumber/cucumber-jvm/issues/735), [#736](https://github.com/cucumber/cucumber-jvm/pull/736) Mykola Gurov)
* [Core] Disregard order of HashMap entries in unit tests ([#739](https://github.com/cucumber/cucumber-jvm/pull/739) mchenryc)
* [Core] Environment variables/properties are aliased. Example: `HELLO_THERE` == `hello.there` (Aslak Hellesøy)
* [Core] The `cucumber-jvm.properties` file is no longer picked up. Use `cucumber.properties` instead (Aslak Hellesøy)
* [Core] Make standard out non-buffered ([#721](https://github.com/cucumber/cucumber-jvm/pull/721) danielhodder)
* [Core] Allow empty doc string and data table entries after token replacement from scenario outlines ([#712](https://github.com/cucumber/cucumber-jvm/issues/712), [#709](https://github.com/cucumber/cucumber-jvm/pull/709), [#713](https://github.com/cucumber/cucumber-jvm/pull/713) Leon Poon, Björn Rasmusson)
* [Guice] New scenario scope for Guice. Non-backwards compatible ([#683](https://github.com/cucumber/cucumber-jvm/pull/683) jakecollins)

## [1.1.7] (2014-05-19)

* [Core] Custom formatters can be instantiated with `java.net.URI`. (Aslak Hellesøy)
* [JRuby,Jython,Rhino,Groovy] Load scripts by absolute path rather than relative so that relative require/import from those scripts works (Aslak Hellesøy)
* [Scala] Support Scala 2.10 and 2.11. Drop support for Scala 2.9. (Aslak Hellesøy).
* [Core] `cucumber.api.cli.Main.run` no longer calls `System.exit`, allowing embedding in other tools (Aslak Hellesøy)

## [1.1.6] (2014-03-24)

* [Guice] Add hookpoints in Cucumber and GuiceFactory ([#634](https://github.com/cucumber/cucumber-jvm/pull/634) Wouter Coekaerts)
* [Core] Fixed concurrency issue ([#333](https://github.com/cucumber/cucumber-jvm/issues/333), [#554](https://github.com/cucumber/cucumber-jvm/pull/554), [#591](https://github.com/cucumber/cucumber-jvm/issues/591), [#661](https://github.com/cucumber/cucumber-jvm/pull/661) Maxime Meriouma-Caron, Limin)
* [Groovy] Use ~/.../ syntax in Groovy snippets ([#663](https://github.com/cucumber/cucumber-jvm/pull/663) Harald Albers, Aslak Hellesøy)
* [Build] Enforce minimum maven version 3.1.1, update plugin and dependency versions ([#690](https://github.com/cucumber/cucumber-jvm/pull/690), [#691](https://github.com/cucumber/cucumber-jvm/pull/691), [#692](https://github.com/cucumber/cucumber-jvm/pull/692) Nayan Hajratwala)
* [Scala] Fixed scala warnings ([#689](https://github.com/cucumber/cucumber-jvm/pull/689) Nayan Hajratwala)
* [Core] Cannot run cucumber test if path to jar files contains exclamation character. ([#685](https://github.com/cucumber/cucumber-jvm/issues/685) Ruslan, Aslak Hellesøy)
* [Gosu] Support for [Gosu](http://gosu-lang.org/) (Aslak Hellesøy)
* [Core] Ensuring features are parsed before formatters are initialised ([#652](https://github.com/cucumber/cucumber-jvm/pull/652) Tim Mullender)
* [Java] Added ability to define custom annotations. ([#628](https://github.com/cucumber/cucumber-jvm/pull/628) slowikps)
* [Core] Added support for SVG images in HTML output ([#624](https://github.com/cucumber/cucumber-jvm/pull/624) agattiker)
* [Scala] Transforming Gherkin tables into java.util.List<T> broken in Scala DSL ([#668](https://github.com/cucumber/cucumber-jvm/issues/668), [#669](https://github.com/cucumber/cucumber-jvm/pull/669) chriswhelan)
* [Clojure] Add tagged Before/After hook support ([#676](https://github.com/cucumber/cucumber-jvm/pull/676) Jeremy Anderson)
* [Core] POJO with nullable enum fields support in tables ([#684](https://github.com/cucumber/cucumber-jvm/pull/684) Mykola Gurov)
* [Core] `DataTable.flatten()` is gone. Use `DataTable.asList(String.class)` instead (Aslak Hellesøy)
* [Core] A DataTable of 2 columns can be turned into a Map excplicitly via `DataTable.asMap()` or by declaring a generic parameter type. Similar to Cucumber-Ruby's `Table#rows_hash` (Aslak Hellesøy)
* [Core] Snippets for quoted arguments changed from from `([^\"]*)` to `(.*?)` to be aligned with Cucumber-Ruby (Aslak Hellesøy)
* [Build] JDK7 is required to build everything. Built jars should still work on JDK6 (Aslak Hellesøy)
* [Core] Fix compilation on JDK7 on OS X. ([#499](https://github.com/cucumber/cucumber-jvm/pull/499), [#487](https://github.com/cucumber/cucumber-jvm/issues/487) Aslak Hellesøy)
* [Andriod] Enable custom test runners to run Cucumber features (to enable usage of the Espresso framework). ([#662](https://github.com/cucumber/cucumber-jvm/issues/662), [#667](https://github.com/cucumber/cucumber-jvm/pull/667) Björn Rasmusson)
* [Core] Expose Scenario name to step definitions. ([#671](https://github.com/cucumber/cucumber-jvm/pull/671) Dominic Fox)
* [Clojure] Fixed bug in the snippet generation that caused an exception. ([#650](https://github.com/cucumber/cucumber-jvm/pull/650) shaolang)
* [Core] More precise handling of the XStream errors. ([#657](https://github.com/cucumber/cucumber-jvm/issues/657), [#658](https://github.com/cucumber/cucumber-jvm/pull/658) Mykola Gurov)
* [Core] Performance improvement: URLOutputStream can write several bytes, not just one-by-one. ([#654](https://github.com/cucumber/cucumber-jvm/issues/654) Aslak Hellesøy)
* [Core] Add support for transposed tables. ([#382](https://github.com/cucumber/cucumber-jvm/issues/382), [#635](https://github.com/cucumber/cucumber-jvm/pull/635), Roberto Lo Giacco)
* [Examples] Fixed concurrency bugs in Webbit Selenium example (Aslak Hellesøy)
* [Core] Fixed thread leak in timeout implementation. ([#639](https://github.com/cucumber/cucumber-jvm/issues/639), [#640](https://github.com/cucumber/cucumber-jvm/pull/640), Nikolay Volnov)
* [TestNG] Allow TestNG Cucumber runner to use composition instead of inheritance. ([#622](https://github.com/cucumber/cucumber-jvm/pull/622) Marty Kube)
* [Core] New Snippet text. ([#618](https://github.com/cucumber/cucumber-jvm/issues/618) Jeff Nyman, Matt Wynne, Aslak Hellesøy)
* [Android] Add command line option support for Android ([#597](https://github.com/cucumber/cucumber-jvm/pull/597), Frieder Bluemle)
* [Android] Add debug support for eclipse ([#613](https://github.com/cucumber/cucumber-jvm/pull/613) Ian Warwick)
* [Core] Make the RerunFormatter handle failures in background and scenario outline examples correctly ([#589](https://github.com/cucumber/cucumber-jvm/pull/589) Björn Rasmusson)
* [Core] Fix stop watch thread safety ([#606](https://github.com/cucumber/cucumber-jvm/pull/606) Dave Bassan)
* [Android] Fix Cucumber reports for cucumber-android ([#605](https://github.com/cucumber/cucumber-jvm/pull/605) Frieder Bluemle)
* [Spring] Fix for tests annotated with @ContextHierarchy ([#590](https://github.com/cucumber/cucumber-jvm/pull/590) Martin Lau)
* [Core] Add error check to scenario outline, add java snippet escaping for `+` and `.` ([#596](https://github.com/cucumber/cucumber-jvm/pull/596) Guy Burton)
* [Rhino] World build and disposal support added to Rhino ([#595](https://github.com/cucumber/cucumber-jvm/pull/595) Rui Figueira)
* [Jython] Fix for DataTable as parameter in Jython steps ([#599](https://github.com/cucumber/cucumber-jvm/issues/599), [#602](https://github.com/cucumber/cucumber-jvm/pull/602) lggroapa, Aslak Hellesøy)
* [Core] Fix and improve CamelCaseFunctionNameSanitizer ([#603](https://github.com/cucumber/cucumber-jvm/pull/603) Frieder Bluemle)
* [Android] improve test reports for cucumber-android ([#598](https://github.com/cucumber/cucumber-jvm/pull/598) Sebastian Gröbler)
* [Core] The JSONFormatter should record before hooks in the next scenario ([#570](https://github.com/cucumber/cucumber-jvm/pull/570) Björn Rasmusson)
* [Core, Java] Log a warning when more than one IoC dependency is found in the classpath ([#594](https://github.com/cucumber/cucumber-jvm/pull/594) Ariel Kogan)
* [JUnit,TestNG] Report summaries and `.cucumber/stepdefs.json` in the same way as the CLI (Aslak Hellesøy)

## [1.1.5] (2013-09-14)

* [Core] There are now three ways to override Cucumber Options. (Aslak Hellesøy)
  * `cucumber.options="..."` passed to the JVM with `-Dcucumber.options="..."`.
  * The environment variable `CUCUMBER_OPTIONS="..."`.
  * A `cucumber-jvm.properties` on the `CLASSPATH` with a `cucumber.options="..."` property.
* [Core] Feature paths and `--glue` in `cucumber.options` clobber defaults rather than appending to them. (Aslak Hellesøy)
* [JRuby] The `GEM_PATH` and `RUBY_VERSION` values will be picked up from `cucumber-jvm.properties` instead of `cucumber-jruby.properties` (Aslak Hellesøy).
* [Core] Step Definition and Hook timeout is now a `long` instead of an `int`. (Aslak Hellesøy)
* [Rhino] Before and After hooks support ([#587](https://github.com/cucumber/cucumber-jvm/pull/587) Rui Figueira)
* [Android] Separate CI job for Android. ([#581](https://github.com/cucumber/cucumber-jvm/issues/581), [#584](https://github.com/cucumber/cucumber-jvm/pull/584) Björn Rasmusson)
* [Android] Add support for Dependency Injection via cucumber-picocontainer, cucumber-guice, cucumber-spring etx. (Aslak Hellesøy)
* [TestNG] Java Calculator TestNG example project ([#579](https://github.com/cucumber/cucumber-jvm/pull/579) Dmytro Chyzhykov)
* [Jython] Access to scenario in Before and After hooks ([#582](https://github.com/cucumber/cucumber-jvm/issues/582) Aslak Hellesøy)
* [Core] Replace placeholders in the Scenario Outline title ([#580](https://github.com/cucumber/cucumber-jvm/pull/580), [#510](https://github.com/cucumber/cucumber-jvm/issues/510) Jamie W. Astin)
* [JUnit/Core] `@cucumber.junit.api.Cucumber.Options` is deprecated in favour of `@cucumber.api.CucumberOptions` ([#549](https://github.com/cucumber/cucumber-jvm/issues/549) Aslak Hellesøy)
* [JUnit] Inherit Information of @Cucumber.Options ([#568](https://github.com/cucumber/cucumber-jvm/issues/568) Klaus Bayrhammer)
* [JUnit] JUnitFormatter does not put required name attribute in testsuite root element ([#480](https://github.com/cucumber/cucumber-jvm/pull/480), [#477](https://github.com/cucumber/cucumber-jvm/issues/477) ericmaxwell2003)
* [Core] Output embedded text in HTML report ([#501](https://github.com/cucumber/cucumber-jvm/pull/501) Tom Dunstan)
* [Core] Fix for Lexing Error message not useful ([#519](https://github.com/cucumber/cucumber-jvm/issues/519), [#523](https://github.com/cucumber/cucumber-jvm/pull/523) Alpar Gal)
* [TestNG] New TestNG integration. ([#441](https://github.com/cucumber/cucumber-jvm/issues/441), [#526](https://github.com/cucumber/cucumber-jvm/pull/526) Dmytro Chyzhykov)
* [Core] Implemented rerun formatter. ([#495](https://github.com/cucumber/cucumber-jvm/issues/495), [#524](https://github.com/cucumber/cucumber-jvm/pull/524) Alpar Gal)
* [Java,Needle] New DI engine: Needle. ([#496](https://github.com/cucumber/cucumber-jvm/issues/496), [#500](https://github.com/cucumber/cucumber-jvm/pull/500) Jan Galinski)
* [Core] Bugfix: StringIndexOutOfBoundsException when optional argument not present. ([#394](https://github.com/cucumber/cucumber-jvm/issues/394), [#558](https://github.com/cucumber/cucumber-jvm/pull/558) Guy Burton)
* [Java, Jython] New `--snippet [underscore|camelcase]` option for more control over snippet style. ([#561](https://github.com/cucumber/cucumber-jvm/pull/561), [302](https://github.com/cucumber/cucumber-jvm/pull/302) Márton Mészáros, Aslak Hellesøy)
* [Windows] Use uri instead of path in CucumberFeature ([#562](https://github.com/cucumber/cucumber-jvm/pull/562) Björn Rasmusson)
* [Android] Better example for Cucumber-Android. ([#547](https://github.com/cucumber/cucumber-jvm/issues/547), [#574](https://github.com/cucumber/cucumber-jvm/issues/574) Maximilian Fellner)
* [Android] Use @CucumberOptions instead of @RunWithCucumber. ([#576](https://github.com/cucumber/cucumber-jvm/issues/576) Maximilian Fellner)
* [Android] Deploy a jar for cucumber-android. ([#573](https://github.com/cucumber/cucumber-jvm/issues/573) Maximilian Fellner, Aslak Hellesøy)

## [1.1.4] (2013-08-11)

* [Core] Fixed a bug where `@XStreamConverter` annotations were ignored (Aslak Hellesøy)
* [Android] New Cucumber-Android module ([#525](https://github.com/cucumber/cucumber-jvm/pull/525) Maximilian Fellner).
* [Build] Deploy maven SNAPSHOT versions from Travis ([#517](https://github.com/cucumber/cucumber-jvm/issues/517), [#528](https://github.com/cucumber/cucumber-jvm/pull/528) Tom Dunstan)
* [Core] JUnitFormatter to mark skipped tests as failures in strict mode ([#543](https://github.com/cucumber/cucumber-jvm/pull/543) Björn Rasmusson)
* [Core] Always cancel timeout at the end of a stepdef, even when it fails. ([#540](https://github.com/cucumber/cucumber-jvm/issues/540) irb1s)
* [Groovy] Updated examples to be more explanatory and groovier syntax ([#537](https://github.com/cucumber/cucumber-jvm/pull/522) Quantoid)
* [PicoContainer,Groovy,JRuby,Jython] Not shading maven artifacts any longer. Gem has a shaded jar though. ([#522](https://github.com/cucumber/cucumber-jvm/pull/522) [#518](https://github.com/cucumber/cucumber-jvm/issues/518) Dmytro Chyzhykov, Aslak Hellesøy)
* [Core] The `json-pretty` formatter is gone, and the `json` formatter is pretty!
* [Spring] New awesome Spring port of The Cucumber Book's chapter 14. ([#508](https://github.com/cucumber/cucumber-jvm/pull/508), [#489](https://github.com/cucumber/cucumber-jvm/pull/489) Dmytro Chyzhykov, Pedro Antonio Souza Viegas)
* [Core] Added `Scenario.getSourceTagNames()`, which is needed to make Capybara work with Cucumber-JRuby ([#504](https://github.com/cucumber/cucumber-jvm/issues/504) Aslak Hellesøy)
* [JRuby] Tagged hooks for JRuby ([#467](https://github.com/cucumber/cucumber-jvm/issues/467) Aslak Hellesøy)
* [Spring] Implementation based on SpringJunit4ClassRunner. ([#448](https://github.com/cucumber/cucumber-jvm/issues/448), [#489](https://github.com/cucumber/cucumber-jvm/pull/489) Pedro Antonio Souza Viegas)
* [Core] Bugfix: Generated regex for ? character is incorrect. ([#494](https://github.com/cucumber/cucumber-jvm/issues/494) Aslak Hellesøy)
* [Core] Improve readability with unanchored regular expressions ([#485](https://github.com/cucumber/cucumber-jvm/pull/485), [#466](https://github.com/cucumber/cucumber-jvm/issues/466) Anton)
* [Core] Throw exception when unsupported command line options are used. ([#482](https://github.com/cucumber/cucumber-jvm/pull/482), [#463](https://github.com/cucumber/cucumber-jvm/issues/463) Klaus Bayrhammer)
* [Scala] Release cucumber-scala for the two most recent minor releases (currently 2.10.2 and 2.9.3) ([#432](https://github.com/cucumber/cucumber-jvm/issues/432), [#462](https://github.com/cucumber/cucumber-jvm/pull/462) Chris Turner)
* [Core] JUnitFormatter: Fix indentation, hook handling and support all-steps-first execution ([#556](https://github.com/cucumber/cucumber-jvm/pull/556) Björn Rasmusson)
* [Core] Make the PrettyFormatter work by revering to all-steps-first execution ([#491](https://github.com/cucumber/cucumber-jvm/issues/491), [#557](https://github.com/cucumber/cucumber-jvm/pull/557) Björn Rasmusson)
* [Core] Test case for the PrettyFormatter. ([#544](https://github.com/cucumber/cucumber-jvm/pull/544) Björn Rasmusson)
* [Core/Junit] Print summary at the end of the run. ([#195](https://github.com/cucumber/cucumber-jvm/issues/195), [#536](https://github.com/cucumber/cucumber-jvm/pull/536) Björn Rasmusson)
* [Core/Examples] Return exit code 0 when no features are found, add example java-no-features. ([#567](https://github.com/cucumber/cucumber-jvm/pull/567) Björn Rasmusson, Dmytro Chyzhykov)

## [1.1.3] (2013-03-10)

* [Core] Added accessors to `TableDiffException`. ([#384](https://github.com/cucumber/cucumber-jvm/issues/384) Aslak Hellesøy)
* [Core] Fixed use of formatter to list all step results in JSON output ([#426](https://github.com/cucumber/cucumber-jvm/pull/426) agattiker)
* [Scala] Add support for DataTable and locale-aware type transformations. ([#443](https://github.com/cucumber/cucumber-jvm/issues/443), [#455](https://github.com/cucumber/cucumber-jvm/pull/455) Matthew Lucas)
* [Groovy] Groovy should throw exception if more then one World registred ([#464](https://github.com/cucumber/cucumber-jvm/pull/464), [#458](https://github.com/cucumber/cucumber-jvm/issues/458) Luxor)
* [Core] Diffing tables doesn't work when delta span multiple lines ([#465](https://github.com/cucumber/cucumber-jvm/pull/465) Gilles Philippart)
* [JRuby] `GEM_PATH` and `RUBY_VERSION` can be set in env var, system property or `cucumber-jruby.properties` resource bundle. (Aslak Hellesøy)
* [JRuby] Wrong CompatVersion passed to JRuby when 1.9 is given ([#415](https://github.com/cucumber/cucumber-jvm/issues/415) David Kowis)
* [Core] Custom Formatter/Reporter's `before` and `after` hook weren't run. (Aslak Hellesøy)
* [Clojure] Clojure backend should define HookDefinition.getLocation(boolean detail) ([#461](https://github.com/cucumber/cucumber-jvm/issues/461), [#471](https://github.com/cucumber/cucumber-jvm/pull/471) Nils Wloka)

## [1.1.2] (2013-01-30)

* [Core] Restore ability to diff with another DataTable ([#413](https://github.com/cucumber/cucumber-jvm/pull/413) Gilles Philippart)
* [Core] Executing a test with the --dry-run option does not skip the @Before or @After annotations ([#424](https://github.com/cucumber/cucumber-jvm/issues/424), [#444](https://github.com/cucumber/cucumber-jvm/pull/444) William Powell)
* [Clojure] Updated lein-cucumber version to 1.0.1 ([#414](https://github.com/cucumber/cucumber-jvm/pull/414) Nils Wloka)
* [JUnit] Upgrade to 4.11 ([#322](https://github.com/cucumber/cucumber-jvm/issues/322) [#445](https://github.com/cucumber/cucumber-jvm/pull/445) Petter Måhlén, Aslak Hellesøy)
* [Spring] Upgrade to 3.2.1.RELEASE (Aslak Hellesøy)
* [Core] Strip command line arguments in case people accidentally invoke `cucumber.api.cli.Main` with arguments that have spaces left and right. (Aslak Hellesøy)
* [Core] Implemented `DataTable.equals()` and `DataTable.hashCode()`. (Aslak Hellesøy)
* [Core] Support `DataTable.toTable(List<String[]>)` and `DataTable.toTable(List<Map<String,String>>)` ([#433](https://github.com/cucumber/cucumber-jvm/issues/433), [#434](https://github.com/cucumber/cucumber-jvm/pull/434) Nicholas Albion, Aslak Hellesøy)
* [Core] Formatters and `--dotcucumber` can now write to a file or an URL (via HTTP PUT). This allows easier distribution of reports. (Aslak Hellesøy)
* [JUnit] Added `@Cucumber.Options.dotcucumber`, allowing metadata to be written from JUnit. Useful for code completion. ([#418](https://github.com/cucumber/cucumber-jvm/issues/418) Aslak Hellesøy)
* [Core] Embedded data fails to display in HTML reports due to invalid string passed from HTMLFormatter ([#412](https://github.com/cucumber/cucumber-jvm/issues/412) Aslak Hellesøy)
* [Scala] Upgrade to scala 2.10.0. (Aslak Hellesøy)
* [Scala] Passing Scenario reference in Before and After hooks ([#431](https://github.com/cucumber/cucumber-jvm/pull/431) Anshul Bajpai)
* [Core] RunCukesTest prevents the execution of other tests ([#304](https://github.com/cucumber/cucumber-jvm/issues/304), [#430](https://github.com/cucumber/cucumber-jvm/pull/430) Mishail)
* [Core] Deprecated `cucumber.runtime.PendingException` in favour of `cucumber.api.PendingException`. (Aslak Hellesøy)
* [Core] New `@cucumber.api.Pending` annotation for custom `Exception` classes that will cause a scenario to be `pending` instead of `failed`. ([#427](https://github.com/cucumber/cucumber-jvm/pull/427) agattiker)
* [Core] `--name 'name with spaces in single quotes'` is working ([#379](https://github.com/cucumber/cucumber-jvm/issues/379), [#429](https://github.com/cucumber/cucumber-jvm/pull/429) William Powell)
* [Examples/Spring] Spring Data JPA based repositories. ([#422](https://github.com/cucumber/cucumber-jvm/pull/422) Dmytro Chyzhykov)
* [Examples/Gradle] Added a Gradle example. ([#446](https://github.com/cucumber/cucumber-jvm/pull/446) Ivan Yatskevich, David Kowis)

## [1.1.1] (2012-10-25)

This release bumps the minor version number from 1.0 to 1.1. This is because there are backwards-incompatible changes.
There shouldn't be anything else that breaks than package renames and a few class renames. The reason for these breaking
changes is to make it more obvious what parts of the API are public and what parts are not. From now on, anything in the
`cucumber.api` package and below is public. If you're importing *any* `cucumber.*` packages that don't start with
`cucumber.api` you're using an internal API, and that might still change in future releases. The goal is to have anything
in `cucumber.api` stable from now on, with proper deprecation warnings in case some APIs still need to change.

* [Scala] Up the cucumber-scala Scala dependencies to 2.10.0-RC1 ([#409](https://github.com/cucumber/cucumber-jvm/pull/409) Chris Turner)
* [JRuby] Upgraded to JRuby 1.7.0 (Aslak Hellesøy)
* [JRuby] I18n stepdefs. `require 'cucumber/api/jruby/en'` or other language. ([#177](https://github.com/cucumber/cucumber-jvm/issues/177) Aslak Hellesøy)
* [JRuby] Calling steps from stepefs now uses the `step` method (Aslak Hellesøy)
* [JRuby] World(module) works (Aslak Hellesøy)
* [JRuby] The DSL no longer leaks into global scope (Aslak Hellesøy)
* [Spring] The `@txn` hooks in the `cucumber.runtime.java.spring.hooks` package have order 100. ([398](https://github.com/cucumber/cucumber-jvm/issues/398) Aslak Hellesøy)
* [Java] The `@Order` annotation is replaced with an `order` property on `@Before` and `@After` (Aslak Hellesøy)
* [Core] Make sure all report files are written with UTF-8 encoding ([402](https://github.com/cucumber/cucumber-jvm/issues/402) MIC, Aslak Hellesøy)
* [Core] HTMLFormatter improvements ([375](https://github.com/cucumber/cucumber-jvm/issues/375), [404](https://github.com/cucumber/cucumber-jvm/issues/404), [283](https://github.com/cucumber/cucumber-jvm/issues/283) Aslak Hellesøy)
* [All] Package reorganisation. Only classes under `cucumber.api` are part of the public (stable) API. Classes in other classes are not part of the API and can change. (Aslak Hellesøy)
* [Core] Improved `Transformer` API (Aslak Hellesøy)
* [Java] Renamed `@DateFormat` to `@Format` (Aslak Hellesøy)
* [Core] Fixed a bug where `-Dcucumber.options="--format pretty"` would fail with the JUnit runner. (Aslak Hellesøy).
* [Core] Scenario Transform header being treated like an object (no bugfix, but added explanation) ([#396](https://github.com/cucumber/cucumber-jvm/issues/396) Aslak Hellesøy)
* [Core] TableDiff with list of pojos: camelcase convert of column names to field names ([#385](https://github.com/cucumber/cucumber-jvm/pull/385) mbusik)
* [Core] Added video/ogg mimetype to embedd videos in the HTMLReport ([#390](https://github.com/cucumber/cucumber-jvm/pull/390) Klaus Bayrhammer)
* [Groovy] Generated Groovy step definitions need backslashes to be escaped ([#391](https://github.com/cucumber/cucumber-jvm/issues/391), [#400](https://github.com/cucumber/cucumber-jvm/pull/400), Martin Hauner)
* [Java] The java module (and all other modules) finally compile on JDK 7 and OS X. (David Kowis, Sébastien Le Callonnec, Aslak Hellesøy)
* [Core] The `cucumber.options` System property will no longer completely override all arguments set in `@Cucumber.Options` or
  on the command line. Instead, it will keep those and only override those that are specified in `cucumber.options`.
  Special cases are `--tags`, `--name` and `path:line`, which will override previous tags/names/lines. To override a boolean
  option (options that don't take arguments like `--monochrome`), use the `--no-` counterpart (`--no-monochrome`). ([#388](https://github.com/cucumber/cucumber-jvm/pull/388) Sébastien Le Callonnec, Aslak Hellesøy)

## [1.0.14] (2012-08-20)

(The 1.0.13 release failed half way through)

* [Core] gherkin.jar, gherkin-jvm-deps.jar and cucumber-jvm-deps.jar are embedded inside cucumber-core.jar (to simplify installation) (Aslak Hellesøy)

## [1.0.12] (2012-08-19)

* [Core] No img data in embeddings using both json and html reports ([#339](https://github.com/cucumber/cucumber-jvm/issues/339) Aslak Hellesøy)
* [Core] JUnit assume failures (`AssumptionViolatedException`) behaves in the same way as pending (`cucumber.runtime.PendingException`) ([#359](https://github.com/cucumber/cucumber-jvm/issues/359) Aslak Hellesøy, Kim Saabye Pedersen)
* [Core] Extend url protocols. This makes it possible to load features and glue from within a container such as Arquilian. ([#360](https://github.com/cucumber/cucumber-jvm/issues/360), [#361](https://github.com/cucumber/cucumber-jvm/pull/361) Logan McGrath)
* [Jython] Jython Before/After Annotations ([#362](https://github.com/cucumber/cucumber-jvm/pull/362) Stephen Abrams)
* [Java] Support for delimited lists in step parameters ([#364](https://github.com/cucumber/cucumber-jvm/issues/364), [#371](https://github.com/cucumber/cucumber-jvm/pull/371) Marquis Wang)
* [Groovy] Load `env.groovy` before other glue code files. ([#374](https://github.com/cucumber/cucumber-jvm/pull/374) Tomas Bezdek)
* [Clojure] Add utilities for reading tables ([#376](https://github.com/cucumber/cucumber-jvm/pull/376) rplevy-draker)

## [1.0.11] (2012-07-06)

* [Core] Added a new `@Transform` annotation and an abstract `Transformer` class giving full control over argument transforms.
* [OpenEJB] Remove log4j need for openejb module ([#355](https://github.com/cucumber/cucumber-jvm/pull/355) rmannibucau)
* [JUnit] JUnit report doesn't correctly report errors ([#315](https://github.com/cucumber/cucumber-jvm/issues/315), [#356](https://github.com/cucumber/cucumber-jvm/pull/356) Kevin Cunningham)

## [1.0.10] (2012-06-20)

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

## [1.0.9] (2012-06-08)

* [Core] Exceptions thrown from a step definition are no longer wrapped in CucumberException. (Aslak Hellesøy)
* [Core] Fixed regression: PendingException was causing steps to fail instead of pending. ([#328](https://github.com/cucumber/cucumber-jvm/issues/328) Aslak Hellesøy)
* [Java] Missing String.format parameters in DefaultJavaObjectFactory ([#336](https://github.com/cucumber/cucumber-jvm/issues/336) paulkrause88, Aslak Hellesøy)
* [Core] Exceptions being swallowed if reported in a Hook ([#133](https://github.com/cucumber/cucumber-jvm/issues/133) David Kowis, Aslak Hellesøy)
* [Core] Added `DataTable.asMaps()` and made all returned lists immutable. (Aslak Hellesøy).
* [Java] The java-helloworld example has a simple example illustrating data tables and doc strings. (Aslak Hellesøy).
* [Core] Run scenarios/features by name ([#233](https://github.com/cucumber/cucumber-jvm/issues/233), [#323](https://github.com/cucumber/cucumber-jvm/pull/323) Klaus Bayrhammer)
* [Jython] Added missing `self` argument in Jython snippets. ([#324](https://github.com/cucumber/cucumber-jvm/issues/324) Aslak Hellesøy)
* [Scala] Fixed regression from v1.0.6 in Scala module - glue code wasn't loaded at all. ([#321](https://github.com/cucumber/cucumber-jvm/issues/321) Aslak Hellesøy)

## [1.0.8] (2012-05-17)

* [Core] Ability to create `DataTable` objects from a List of objects while specifying what header columns (fields) to use (Aslak Hellesøy)
* [Core] `table.diff(listOfPojos)` no longer spuriously fails because of pseudo-random column/field ordering (Aslak Hellesøy)
* [Core] Tables with empty cells make the column disappear ([#320](https://github.com/cucumber/cucumber-jvm/pull/320) Aslak Hellesøy, Gilles Philippart)
* [Java] Add 'throws Throwable' to generated Java stepdef snippets ([#318](https://github.com/cucumber/cucumber-jvm/issues/318), [#319](https://github.com/cucumber/cucumber-jvm/pull/319) Petter Måhlén)
* [Core] Remove forced UTC timezone. ([#317](https://github.com/cucumber/cucumber-jvm/pull/317) Gilles Philippart)
* [Core] Options (Command line or `@Cucumber.Options`) can be overriden with the `cucumber.options` system property. (Aslak Hellesøy)

## [1.0.7] (2012-05-10)

* [Java] cucumber-java lazily creates instances, just like the other DI containers. (Aslak Hellesøy)
* [Core] Throw an exception if a glue or feature path doesn't exist (i.e. neither file nor directory) (Aslak Hellesøy)

## [1.0.6] (2012-05-03)

* [JUnit] Scenarios with skipped, pending or undefined steps show up as yellow in IDEA and Eclipse (They used to be green while the steps were yellow). (Aslak Hellesøy)
* [Core] Loading features and glue code from the `CLASSPATH` can be done with `classpath:my/path` ([#312](https://github.com/cucumber/cucumber-jvm/issues/312) Aslak Hellesøy)
* [Clojure] Clojure example can't find cuke_steps.clj ([#291](https://github.com/cucumber/cucumber-jvm/issues/291), [#309](https://github.com/cucumber/cucumber-jvm/pull/309) Nils Wloka)

## [1.0.4] (2012-04-23)

* [Core] Ability to specify line numbers: `@Cucumber.Options(features = "my/nice.feature:2:10")` ([#234](https://github.com/cucumber/cucumber-jvm/issues/234) Aslak Hellesøy)
* [WebDriver] Improved example that shows how to reuse a driver for the entire JVM. (Aslak Hellesøy)
* [Core] Allow custom @XStreamConverter to be used on regular arguments - not just table arguments. (Aslak Hellesøy)
* [Groovy] fixed & simplified groovy step snippets ([#303](https://github.com/cucumber/cucumber-jvm/pull/303) Martin Hauner)
* [Java] Detect subclassing in glue code and report to the user that it's illegal. ([#301](https://github.com/cucumber/cucumber-jvm/issues/301) Aslak Hellesøy)
* [Core] Friendlier error message when XStream fails to assign null to primitive fields ([#296](https://github.com/cucumber/cucumber-jvm/issues/296) Aslak Hellesøy)

## [1.0.3] (2012-04-19)

* [Core] Friendlier error message when XStream fails conversion ([#296](https://github.com/cucumber/cucumber-jvm/issues/296) Aslak Hellesøy)
* [Core] Empty strings from matched steps and table cells are converted to `null`. This means boxed types must be used if you intend to have empty strings. (Aslak Hellesøy)
* [Core] Implement --strict ([#196](https://github.com/cucumber/cucumber-jvm/issues/196), [#284](https://github.com/cucumber/cucumber-jvm/pull/284) Klaus Bayrhammer)
* [Clojure] Cucumber-clojure adding after hook to before ([#294](https://github.com/cucumber/cucumber-jvm/pull/294) Daniel E. Renfer)
* [Java] Show code source for Java step definitions in case of duplicates or ambiguous stepdefs. (Aslak Hellesøy).
* [Groovy] Arity mismatch can be avoided by explicitly declaring an empty list of closure parameters. ([#297](https://github.com/cucumber/cucumber-jvm/issues/297) Aslak Hellesøy)
* [Core] Added DataTable.toTable(List<?> other) for creating a new table. Handy for printing a table when diffing isn't helpful. (Aslak Hellesøy)

## [1.0.2] (2012-04-03)

* [Java] Snippets using a table have a hint about how to use List<YourClass>. (Aslak Hellesøy)
* [Java] Don't convert paths to package names - instead throw an exception. This helps people avoid mistakes. (Aslak Hellesøy)
* [Scala] Fixed generated Scala snippets ([#282](https://github.com/cucumber/cucumber-jvm/pull/282) pawel-s)
* [JUnit] Automatically turn off ANSI colours when launched from IDEA. (Aslak Hellesøy)

## [1.0.1] (2012-03-29)

* [Clojure] Fix quoting of generated Clojure snippets ([#277](https://github.com/cucumber/cucumber-jvm/pull/277) Michael van Acken)
* [Guice] Guice in multi module/class loader setup ([#278](https://github.com/cucumber/cucumber-jvm/pull/278) Matt Nathan)
* [JUnit] Background steps show up correctly in IntelliJ ([#276](https://github.com/cucumber/cucumber-jvm/issues/276) Aslak Hellesøy)

## [1.0.0] (2012-03-27)

* [Docs] Added Cuke4Duke migration notes to README ([#239](https://github.com/cucumber/cucumber-jvm/pull/239) coldbloodedtx)
* [Core] Added --monochrome flag, allowing monochrome output for certain formatters ([#221](https://github.com/cucumber/cucumber-jvm/issues/221) Aslak Hellesøy)
* [Core] Added a usage formatter ([#207](https://github.com/cucumber/cucumber-jvm/issues/207), [#214](https://github.com/cucumber/cucumber-jvm/pull/214) Klaus Bayrhammer)
* [Core] JavaScript-Error in HTML-Report when using ScenarioResult.write ([#254](https://github.com/cucumber/cucumber-jvm/issues/254) Aslak Hellesøy)
* [Java] Add support for enums in stepdefs ([#217](https://github.com/cucumber/cucumber-jvm/issues/217), [#240](https://github.com/cucumber/cucumber-jvm/pull/240) Gilles Philippart)
* [Core] Help text for CLI. ([#142](https://github.com/cucumber/cucumber-jvm/issues/142) Aslak Hellesøy)
* [JUnit] Eclipse JUnit reports inaccurate run count ([#263](https://github.com/cucumber/cucumber-jvm/issues/263), [#274](https://github.com/cucumber/cucumber-jvm/pull/274) dgradl)

## [1.0.0.RC24] (2012-03-22)

* [Core] Understandable error message if a formatter needs output location. ([#148](https://github.com/cucumber/cucumber-jvm/issues/148), [#232](https://github.com/cucumber/cucumber-jvm/issues/232), [#269](https://github.com/cucumber/cucumber-jvm/issues/269) Aslak Hellesøy)
* [JUnit] Running with JUnit uses a null formatter by default (instead of a progress formatter). (Aslak Hellesøy)
* [Clojure] Fix release artifacts so cucumber-clojure can be released. ([#270](https://github.com/cucumber/cucumber-jvm/issues/270) Aslak Hellesøy)
* [Java] The @Pending annotation no longer exists. Throw a PendingException instead ([#271](https://github.com/cucumber/cucumber-jvm/issues/271) Aslak Hellesøy)

## [1.0.0.RC23] (2012-03-20)

* [JUnit] CucumberException when running Cucumber with Jacoco code coverage ([#258](https://github.com/cucumber/cucumber-jvm/issues/258) Jan Stamer, Aslak Hellesøy)
* [Scala] Scala Javadoc problems with build ([#231](https://github.com/cucumber/cucumber-jvm/issues/231) Aslak Hellesøy)

## [1.0.0.RC22] (2012-03-20)

* [Java] Snippets for DataTable include a hint about using List<YourType>, so people discover this neat technique (Aslak Hellesøy)
* [Core] Support DocString and DataTable in generated snippets ([#227](https://github.com/cucumber/cucumber-jvm/issues/227) Aslak Hellesøy)
* [Core] Fix broken --tags option (and get rid of JCommander for CLI parsing). ([#266](https://github.com/cucumber/cucumber-jvm/issues/266) Aslak Hellesøy)
* [Clojure] Make Clojure DSL syntax cleaner ([#244](https://github.com/cucumber/cucumber-jvm/issues/244) [#267](https://github.com/cucumber/cucumber-jvm/pull/267) rplevy-draker)
* [Clojure] Native Clojure backend ([#138](https://github.com/cucumber/cucumber-jvm/pull/138) [#265](https://github.com/cucumber/cucumber-jvm/pull/265) Kevin Downey, Nils Wloka)
* [JUnit] Added `format` attribute to `@Cucumber.Options` (Aslak Hellesøy)

## [1.0.0.RC21] (2012-03-18)

* [Core] Ignore duplicate features instead of throwing exception. ([#259](https://github.com/cucumber/cucumber-jvm/issues/259) Aslak Hellesøy)
* [Core] Wrong message when runner on a non existing tag on feature ([#245](https://github.com/cucumber/cucumber-jvm/issues/245) Aslak Hellesøy, Jérémy Goupil)
* [Groovy, JRuby, Rhino] Make sure UTF-8 encoding is used everywhere ([#251](https://github.com/cucumber/cucumber-jvm/issues/251) Aslak Hellesøy)
* [Core, Cloure] Fixed StepDefinitionMatch to work with StepDefinitions that return null for getParameterTypes ([#250](https://github.com/cucumber/cucumber-jvm/issues/250), [#255](https://github.com/cucumber/cucumber-jvm/pull/255) Nils Wloka)
* [Java] Open up the `JavaBackend` API to ease integration from other tools ([#257](https://github.com/cucumber/cucumber-jvm/pull/257) Aslak Hellesøy).
* [Java] Inheritance in glue classes (stepdefs and hooks) is no longer supported - it causes too many problems. (Aslak Hellesøy).
* [JUnit] `@Cucumber.Options` annotation replaces `@Feature` annotation ([#160](https://github.com/cucumber/cucumber-jvm/issues/160) Aslak Hellesøy)
* [Spring] Slow Spring context performance ([#241](https://github.com/cucumber/cucumber-jvm/issues/241), [#242](https://github.com/cucumber/cucumber-jvm/pull/242) Vladimir Klyushnikov)
* [Core] Support for java.util.Calendar arguments in stepdefs. (Aslak Hellesøy)

## [1.0.0.RC20] (2012-02-29)

* [JUnit] Improved JUnit runner. ([#107](https://github.com/cucumber/cucumber-jvm/issues/107), [#211](https://github.com/cucumber/cucumber-jvm/issues/211), [#216](https://github.com/cucumber/cucumber-jvm/pull/216) Giso Deutschmann)
* [Core] Stacktrace filtering filters away too much. ([#228](https://github.com/cucumber/cucumber-jvm/issues/228) Aslak Hellesøy)
* [Groovy] Fix native Groovy cucumber CLI ([#212](https://github.com/cucumber/cucumber-jvm/issues/212) Martin Hauner)
* [Core] Indeterministic feature ordering on Unix ([#224](https://github.com/cucumber/cucumber-jvm/issues/224) hutchy2570)
* [JUnit] New JUnitFormatter (--format junit) that outputs Ant-style JUnit XML. ([#226](https://github.com/cucumber/cucumber-jvm/pull/226), [#171](https://github.com/cucumber/cucumber-jvm/issues/171) Vladimir Miguro)

## [1.0.0.RC16] (2012-02-20)

* [Core] Embed text and images in reports. ([#205](https://github.com/cucumber/cucumber-jvm/issues/205) Aslak Hellesøy)
* [Core] Detect duplicate step definitions. (Aslak Hellesøy)
* [Java] Auto-generated step definitions should escape dollar signs / other regex chars ([#204](https://github.com/cucumber/cucumber-jvm/issues/204), [#215](https://github.com/cucumber/cucumber-jvm/pull/215) Ian Dees)
* [Core] Scenario Outlines work with tagged hooks. ([#209](https://github.com/cucumber/cucumber-jvm/issues/209), [#210](https://github.com/cucumber/cucumber-jvm/issues/210) Aslak Hellesøy)
* [Spring] Allowed customization of Spring step definitions context ([#203](https://github.com/cucumber/cucumber-jvm/pull/203) Vladimir Klyushnikov)
* [Core] Ambiguous step definitions don't cause Cucumber to blow up, they just fail the step. (Aslak Hellesøy)
* [Java] Fixed NullPointerException in ClasspathMethodScanner ([#201](https://github.com/cucumber/cucumber-jvm/pull/201) Vladimir Klyushnikov)
* [Groovy] Compiled Groovy stepdef scripts are found as well as source ones (Aslak Hellesøy)
* [Jython] I18n translations for most languages. Languages that can't be transformed to ASCII are excluded. ([#176](https://github.com/cucumber/cucumber-jvm/issues/176), [#197](https://github.com/cucumber/cucumber-jvm/pull/197) Stephen Abrams)

## [1.0.0.RC15] (2012-02-07)

* [Java] You must use `cucumber.runtime.xstream` instead of `com.thoughtworks.xstream` for custom converters.
* [Core] XStream and Diffutils are now packaged inside the cucumber-core jar under new package names. ([#179](https://github.com/cucumber/cucumber-jvm/issues/179) Aslak Hellesøy)
* [Core] Fail if no features are found ([#163](https://github.com/cucumber/cucumber-jvm/issues/163) Aslak Hellesøy)
* [Core] Fail if duplicate features are detected ([#165](https://github.com/cucumber/cucumber-jvm/issues/165) Aslak Hellesøy)

## [1.0.0.RC14] (2012-02-06)

* [Core] HTML formatter produces invalid page if no features ([#191](https://github.com/cucumber/cucumber-jvm/issues/191) Paolo Ambrosio)
* [Core] i18n java snippets for undefined steps are always generated with @Given annotation ([#184](https://github.com/cucumber/cucumber-jvm/issues/184) Vladimir Klyushnikov)
* [JUnit] Enhanced JUnit Exception Reporting ([#185](https://github.com/cucumber/cucumber-jvm/pull/185) Klaus Bayrhammer)
* [Guice] Constructor dependency resolution causes errors in GuiceFactory ([#189](https://github.com/cucumber/cucumber-jvm/issues/189) Matt Nathan)

## [1.0.0.RC13] (2012-01-26)

* [Clojure] Fixed hooks ([#175](https://github.com/cucumber/cucumber-jvm/pull/175) Ronaldo M. Ferraz)
* [Core] Properly flush and close formatters ([#173](https://github.com/cucumber/cucumber-jvm/pull/173) Aslak Hellesøy, David Kowis)
* [Core] Use Gherkin's internal Gson (Aslak Hellesøy)
* [JUnit] Better reporting of Before and After blocks (Aslak Hellesøy)
* [Core] Bugfix: Scenario Outlines failing ([#170](https://github.com/cucumber/cucumber-jvm/issues/170) David Kowis, Aslak Hellesøy)
* [OpenEJB] It's back (was excluded from previous releases because it depended on unreleased libs). (Aslak Hellesøy)

## [1.0.0.RC12] (2012-01-23)

* [JUnit] Tagged hooks are executed properly (Aslak Hellesøy)
* [JRuby] Better support for World blocks ([#166](https://github.com/cucumber/cucumber-jvm/pull/166) David Kowis)
* [Java] GluePath can be a package name ([#164](https://github.com/cucumber/cucumber-jvm/issues/164) Aslak Hellesøy)
* [Build] Fixed subtle path issues on Windows
* [Build] Fixed Build Failure: Cucumber-JVM: Scala (FAILURE) ([#167](https://github.com/cucumber/cucumber-jvm/issues/167) Aslak Hellesøy)

## [1.0.0.RC11] (2012-01-21)

* [Build] The build is Maven-based again. It turned out to be the best choice.
* [Scala] The Scala module is back to life. ([#154](https://github.com/cucumber/cucumber-jvm/issues/154) Jon-Anders Teigen)
* [Build] The build should work on Windows again. ([#154](https://github.com/cucumber/cucumber-jvm/issues/154) Aslak Hellesøy)

## 1.0.0.RC6 (2012-01-17)

* [Build] Maven pom.xml files are back (generated from ivy.xml). Ant+Ivy still needed for bootstrapping.

## 1.0.0.RC5 (2012-01-17)

* [Clojure] Snippets use single quote instead of double quote for comments.
* [All] Stepdefs in jars were not loaded correctly on Windows. ([#139](https://github.com/cucumber/cucumber-jvm/issues/139))
* [Build] Fixed repeated Ant builds. ([#141](https://github.com/cucumber/cucumber-jvm/issues/141))
* [Build] Push to local maven repo. ([#143](https://github.com/cucumber/cucumber-jvm/issues/143))

## 1.0.0.RC4 (2012-01-16)

* [Build] Fixed transitive dependencies in POM files. ([#140](https://github.com/cucumber/cucumber-jvm/issues/140))
* [Build] Use a dot (not a hyphen) in RC version names. Required for JRuby gem.
* [Build] Started tagging repo after release.

## 1.0.0-RC3 (2012-01-14)

* First proper release

<!-- Releases -->
[Unreleased]: https://github.com/cucumber/cucumber-jvm/compare/v6.9.1...main
[6.9.1]:  https://github.com/cucumber/cucumber-jvm/compare/v6.9.0...v6.9.1
[6.9.0]:  https://github.com/cucumber/cucumber-jvm/compare/v6.8.2...v6.9.0
[6.8.2]:  https://github.com/cucumber/cucumber-jvm/compare/v6.8.1...v6.8.2
[6.8.1]:  https://github.com/cucumber/cucumber-jvm/compare/v6.8.0...v6.8.1
[6.8.0]:  https://github.com/cucumber/cucumber-jvm/compare/v6.7.0...v6.8.0
[6.7.0]:  https://github.com/cucumber/cucumber-jvm/compare/v6.6.1...v6.7.0
[6.6.1]:  https://github.com/cucumber/cucumber-jvm/compare/v6.6.0...v6.6.1
[6.6.0]:  https://github.com/cucumber/cucumber-jvm/compare/v6.5.1...v6.6.0
[6.5.1]:  https://github.com/cucumber/cucumber-jvm/compare/v6.5.0...v6.5.1
[6.5.0]:  https://github.com/cucumber/cucumber-jvm/compare/v6.4.0...v6.5.0
[6.4.0]:  https://github.com/cucumber/cucumber-jvm/compare/v6.3.0...v6.4.0
[6.3.0]:  https://github.com/cucumber/cucumber-jvm/compare/v6.2.2...v6.3.0
[6.2.2]:  https://github.com/cucumber/cucumber-jvm/compare/v6.2.1...v6.2.2
[6.2.1]:  https://github.com/cucumber/cucumber-jvm/compare/v6.2.0...v6.2.1
[6.2.0]:  https://github.com/cucumber/cucumber-jvm/compare/v6.1.2...v6.2.0
[6.1.2]:  https://github.com/cucumber/cucumber-jvm/compare/v6.1.1...v6.1.2
[6.1.1]:  https://github.com/cucumber/cucumber-jvm/compare/v6.1.0...v6.1.1
[6.1.0]:  https://github.com/cucumber/cucumber-jvm/compare/v6.0.0...v6.1.0
[6.0.0]:  https://github.com/cucumber/cucumber-jvm/compare/v6.0.0-RC2...v6.0.0
[6.0.0-RC2]:  https://github.com/cucumber/cucumber-jvm/compare/v6.0.0-RC1...v6.0.0-RC2
[6.0.0-RC1]:  https://github.com/cucumber/cucumber-jvm/compare/v5.7.0...v6.0.0-RC1
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
[3.0.2]:      https://github.com/cucumber/cucumber-jvm/compare/v3.0.1...v3.0.2
[3.0.1]:      https://github.com/cucumber/cucumber-jvm/compare/v3.0.0...v3.0.1
[3.0.0]:      https://github.com/cucumber/cucumber-jvm/compare/v2.4.0...v3.0.0
[2.4.0]:      https://github.com/cucumber/cucumber-jvm/compare/v2.3.1...v2.4.0
[2.3.1]:      https://github.com/cucumber/cucumber-jvm/compare/v2.3.0...v2.3.1
[2.3.0]:      https://github.com/cucumber/cucumber-jvm/compare/v2.2.0...v2.3.0
[2.2.0]:      https://github.com/cucumber/cucumber-jvm/compare/v2.1.0...v2.2.0
[2.1.0]:      https://github.com/cucumber/cucumber-jvm/compare/v2.0.1...v2.1.0
[2.0.1]:      https://github.com/cucumber/cucumber-jvm/compare/v2.0.0...v2.0.1
[2.0.0]:      https://github.com/cucumber/cucumber-jvm/compare/v1.2.5...v2.0.0
[1.2.6]:      https://github.com/cucumber/cucumber-jvm/compare/v1.2.5...v1.2.6
[1.2.5]:      https://github.com/cucumber/cucumber-jvm/compare/v1.2.4...v1.2.5
[1.2.4]:      https://github.com/cucumber/cucumber-jvm/compare/v1.2.3...v1.2.4
[1.2.3]:      https://github.com/cucumber/cucumber-jvm/compare/v1.2.2...v1.2.3
[1.2.2]:      https://github.com/cucumber/cucumber-jvm/compare/v1.2.0...v1.2.2
[1.2.0]:      https://github.com/cucumber/cucumber-jvm/compare/v1.1.8...v1.2.0
[1.1.8]:      https://github.com/cucumber/cucumber-jvm/compare/v1.1.7...v1.1.8
[1.1.7]:      https://github.com/cucumber/cucumber-jvm/compare/v1.1.6...v1.1.7
[1.1.6]:      https://github.com/cucumber/cucumber-jvm/compare/v1.1.5...v1.1.6
[1.1.5]:      https://github.com/cucumber/cucumber-jvm/compare/v1.1.4...v1.1.5
[1.1.4]:      https://github.com/cucumber/cucumber-jvm/compare/v1.1.3...v1.1.4
[1.1.3]:      https://github.com/cucumber/cucumber-jvm/compare/v1.1.2...v1.1.3
[1.1.2]:      https://github.com/cucumber/cucumber-jvm/compare/v1.1.1...v1.1.2
[1.1.1]:      https://github.com/cucumber/cucumber-jvm/compare/v1.0.14...1.1.1
[1.0.14]:     https://github.com/cucumber/cucumber-jvm/compare/v1.0.12...v1.0.14
[1.0.12]:     https://github.com/cucumber/cucumber-jvm/compare/v1.0.11...v1.0.12
[1.0.11]:     https://github.com/cucumber/cucumber-jvm/compare/v1.0.10...v1.0.11
[1.0.10]:     https://github.com/cucumber/cucumber-jvm/compare/v1.0.9...v1.0.10
[1.0.9]:      https://github.com/cucumber/cucumber-jvm/compare/v1.0.8...v1.0.9
[1.0.8]:      https://github.com/cucumber/cucumber-jvm/compare/v1.0.7...v1.0.8
[1.0.7]:      https://github.com/cucumber/cucumber-jvm/compare/v1.0.6...v1.0.7
[1.0.6]:      https://github.com/cucumber/cucumber-jvm/compare/v1.0.4...v1.0.6
[1.0.4]:      https://github.com/cucumber/cucumber-jvm/compare/v1.0.3...v1.0.4
[1.0.3]:      https://github.com/cucumber/cucumber-jvm/compare/v1.0.2...v1.0.3
[1.0.2]:      https://github.com/cucumber/cucumber-jvm/compare/v1.0.1...v1.0.2
[1.0.1]:      https://github.com/cucumber/cucumber-jvm/compare/v1.0.0...v1.0.1
[1.0.0]:      https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC24...v1.0.0
[1.0.0.RC24]: https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC23...v1.0.0.RC24
[1.0.0.RC23]: https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC22...v1.0.0.RC23
[1.0.0.RC22]: https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC21...v1.0.0.RC22
[1.0.0.RC21]: https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC20...v1.0.0.RC21
[1.0.0.RC20]: https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC16...v1.0.0.RC20
[1.0.0.RC16]: https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC15...v1.0.0.RC16
[1.0.0.RC15]: https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC14...v1.0.0.RC15
[1.0.0.RC14]: https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC13...v1.0.0.RC14
[1.0.0.RC13]: https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC12...v1.0.0.RC13
[1.0.0.RC12]: https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC11...v1.0.0.RC12
[1.0.0.RC11]: https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC6...v1.0.0.RC11
[1.0.0.RC6]:  https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC6...v1.0.0.RC6
[1.0.0.RC5]:  https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC6...v1.0.0.RC5
[1.0.0.RC4]:  https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC6...v1.0.0.RC4
[1.0.0-RC3]:  https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC6...v1.0.0.RC3
