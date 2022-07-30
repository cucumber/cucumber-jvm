# Changelog
This file documents all notable changes for v6.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

----

## [6.11.0] (2021-08-05)

### Added
* [TestNG] Add `CucumberPropertiesProvider` to allow properties from `testng.xml` to be used ([#2351](https://github.com/cucumber/cucumber-jvm/pull/2351) Gayan Sandaruwan)

### Changed
* [JUnit Platform] Update dependency org.junit.platform:junit-platform-engine to v1.7.2
* [Core] Update dependency org.apiguardian:apiguardian-api to v1.1.2

### Fixed
 * [Core] Fixed reports banner to point to [new docs](https://cucumber.io/docs/cucumber/environment-variables/) about environment variables
 * [Core] Remove `--add-plugin` alternate name from USAGE.txt ([#2319](https://github.com/cucumber/cucumber-jvm/pull/2319) ebreck)

## [6.10.4] (2021-05-13)

### Fixed
 * [Core] Upgraded `vis-timeline` to v7.4.8
   - Fixes CVE-2020-28487

## [6.10.3] (2021-04-14)

### Fixed
 * [Gherkin Messages] Rules can be tagged ([cucumber/#1356](https://github.com/cucumber/cucumber/pull/1356) Gáspár Nagy, Seb Rose, Björn Rasmusson, Wannes Fransen)
 * [Java8] `cucumber-java8` works on Java 12+ ([jhalterman/typetools/#66](https://github.com/jhalterman/typetools/pull/66) Nuclearfarts)

## [6.10.2] (2021-03-15)

### Fixed
 * [Core] Mark pending steps as failed in teamcity plugin ([#2264](https://github.com/cucumber/cucumber-jvm/pull/2264)) M.P. Korstanje)

## [6.10.1] (2021-03-08)

### Fixed
 * [Cdi2] Correctly cast the UnmanagedInstance values ([#2242](https://github.com/cucumber/cucumber-jvm/pull/2242), [#2244](https://github.com/cucumber/cucumber-jvm/pull/2244) Daniel Beland)
 * [Cdi2] Add step definitions as beans when not discovered ([#2248](https://github.com/cucumber/cucumber-jvm/pull/2248)) Daniel Beland, M.P. Korstanje)
 * [Jakarta Cdi] Correctly cast the UnmanagedInstance values ([#2242](https://github.com/cucumber/cucumber-jvm/pull/2242), [#2248](https://github.com/cucumber/cucumber-jvm/pull/2248) Daniel Beland)
 * [Jakarta Cdi] Add step definitions as beans when not discovered ([#2248](https://github.com/cucumber/cucumber-jvm/pull/2248)) Daniel Beland, M.P. Korstanje)

## [6.10.0] (2021-02-14)

### Changed
 * Upgraded various internal dependencies
    - create-meta v3.0.0
    - gherkin v17.0.1
    - messages v14.0.1
    - html-formatter v12.0.0

### Deprecated
 * [Java] Deprecated `io.cucumber.java.tl` in favour of `io.cucumber.java.te` ([cucumber/#1238](https://github.com/cucumber/cucumber/pull/1238) Nvmkpk)
 * [Java8] Deprecated `io.cucumber.java8.Tl`. in favour of `io.cucumber.java8.Te` ([cucumber/#1238](https://github.com/cucumber/cucumber/pull/1238) Nvmkpk)
 * [Core] Deprecated `# language: tl` in favour of `# language: te`.  

### Fixed
 * [Core] Pass class loader to ServiceLoader.load invocations ([#2220](https://github.com/cucumber/cucumber-jvm/issues/2220) M.P. Korstanje)
 * [Core] Log warnings when classes or resource could not be loaded ([#2235](https://github.com/cucumber/cucumber-jvm/issues/2235) M.P. Korstanje)
 * [Core] Improve undefined step reporting ([#2208](https://github.com/cucumber/cucumber-jvm/issues/2208) M.P. Korstanje)
 * [Core] Log warnings when resources and classes could not be loaded ([#2235](https://github.com/cucumber/cucumber-jvm/issues/2235) M.P. Korstanje)

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

<!-- Releases -->
[6.11.0]:  https://github.com/cucumber/cucumber-jvm/compare/v6.10.4...v6.11.0
[6.10.4]:  https://github.com/cucumber/cucumber-jvm/compare/v6.10.3...v6.10.4
[6.10.3]:  https://github.com/cucumber/cucumber-jvm/compare/v6.10.2...v6.10.3
[6.10.2]:  https://github.com/cucumber/cucumber-jvm/compare/v6.10.1...v6.10.2
[6.10.1]:  https://github.com/cucumber/cucumber-jvm/compare/v6.10.0...v6.10.1
[6.10.0]:  https://github.com/cucumber/cucumber-jvm/compare/v6.9.1...v6.10.0
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
