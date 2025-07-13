# Changelog

All notable changes to the current version this project will be documented in
this file. For previous versions see the [release-notes archive](release-notes).

For migration instructions from previous major version and a long formF
explanation of noteworthy changes see the [Release Announcement](release-notes/v7.0.0.md).

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- [JUnit Platform Engine] Add `cucumber.junit-platform.discovery.as-root-engine` to work around SBT issues ([#3023](https://github.com/cucumber/cucumber-jvm/pull/3023) M.P. Korstanje)

### Fixed
- [JUnit Platform Engine] Don't use Java 9+ APIs ([#3025](https://github.com/cucumber/cucumber-jvm/pull/3025) M.P. Korstanje)
- [JUnit Platform Engine] Implement toString on custom DiscoverySelectors
  [Core] Fix incomplete id for scenarios under rules in json output ([#3026](https://github.com/cucumber/cucumber-jvm/pull/3026) M.P. Korstanje)

## [7.25.0] - 2025-07-10
### Changed
- [Core] Add status icons to pretty formatter ([pretty-formatter/#5](https://github.com/cucumber/pretty-formatter/pull/5))

## [7.24.0] - 2025-07-07
### Added
- [JUnit Platform Engine] Option to include a parameterized scenario name only if the scenario is parameterized ([#2835](https://github.com/cucumber/cucumber-jvm/pull/2835) M.P. Korstanje)
- [JUnit Platform Engine] Option to order features and scenarios ([#2835](https://github.com/cucumber/cucumber-jvm/pull/2835) M.P. Korstanje)
- [JUnit Platform Engine] Log discovery issues when a classpath resource selector is (e.g. `@SelectClasspathResource`) is used to select a directory. ([#2835](https://github.com/cucumber/cucumber-jvm/pull/2835) M.P. Korstanje)

### Changed
- [JUnit Platform Engine] Use JUnit's `EngineDiscoveryRequestResolver` to resolve classpath based resources. ([#2835](https://github.com/cucumber/cucumber-jvm/pull/2835) M.P. Korstanje)
- [JUnit Platform Engine] Use JUnit Platform 1.13.3 (JUnit Jupiter 5.13.3)
- [Core] Use a message based [Pretty Formatter](https://github.com/cucumber/pretty-formatter) ([#2835](https://github.com/cucumber/cucumber-jvm/pull/3012) M.P. Korstanje)
- [Core] Update dependency io.cucumber:gherkin to v33.0.0
- [Core] Update dependency io.cucumber:messages to v28.2.0
- [Core] Update dependency io.cucumber:html-formatter to v21.13.0
- [Core] Update dependency io.cucumber:junit-xml-formatter to v0.8.0
- [Core] Update dependency io.cucumber:query to v13.4.0
- [Core] Update dependency io.cucumber:testng-xml-formatter to v0.4.1

### Deprecated
- [JUnit] Deprecate `cucumber-junit` in favour of `cucumber-junit-platform-engine` ([#2835](https://github.com/cucumber/cucumber-jvm/pull/3016) M.P. Korstanje)

### Fixed
- [JUnit Platform Engine] Log discovery issues for feature files with parse errors. ([#2835](https://github.com/cucumber/cucumber-jvm/pull/2835) M.P. Korstanje)

## [7.23.0] - 2025-05-29
### Added
- [JUnit Platform Engine, TestNG] Remove framework elements from `UndefinedStepException` stacktrace ([#3002](https://github.com/cucumber/cucumber-jvm/pull/3002) M.P. Korstanje)
- [JUnit Platform Engine] Add `surefire` naming strategy ([#3003](https://github.com/cucumber/cucumber-jvm/pull/3003) M.P. Korstanje)

### Changed
- [JUnit Platform Engine] Use `number-and-pickle-if-parameterized` example naming strategy by default ([#3004](https://github.com/cucumber/cucumber-jvm/pull/3004) M.P. Korstanje)

## [7.22.2] - 2025-05-12
### Changed
- [Archetype] Assume new projects are created with at least Java 17

### Fixed
- [Core] Convert parameterized objects with `@DefaultDataTableEntryTransformer` ([#2995](https://github.com/cucumber/cucumber-jvm/pull/2995) Jean Tissot)

## [7.22.1] - 2025-04-24
### Changed
- [JUnit Platform Engine] Use JUnit Platform 1.12.2 (JUnit Jupiter 5.12.2)

### Fixed
- [Core] Fix issue with hook steps not being rendered in html report [react-components/#379](https://github.com/cucumber/react-components/pull/379)

## [7.22.0] - 2025-04-05
### Changed
- [Core] Improved Gherkin parser performance ([gherkin/#372](https://github.com/cucumber/gherkin/pull/372) M.P. Korstanje & Julien Kronegg)
- [Core] Improved caching glue performance ([#2971](https://github.com/cucumber/cucumber-jvm/pull/2971) M.P. Korstanje & Julien Kronegg)
- [Java, Java8] Significantly reduced number of emitted step- and hook-definition messages ([#2971](https://github.com/cucumber/cucumber-jvm/pull/2971) M.P. Korstanje & Julien Kronegg)
- [Core] Removed workarounds to limit size of html report ([#2971](https://github.com/cucumber/cucumber-jvm/pull/2971) M.P. Korstanje & Julien Kronegg)
- [JUnit Platform Engine] Use JUnit Platform 1.12.0 (JUnit Jupiter 5.12.0)

### Deprecated
- [Core] Deprecated `ScenarioScoped` glue ([#2971](https://github.com/cucumber/cucumber-jvm/pull/2971) M.P. Korstanje & Julien Kronegg)

### Fixed
- [Core] Remove duplications in steps in html report due to empty parameters ([react-components/#373](https://github.com/cucumber/react-components/pull/373))

## [7.21.1] - 2025-02-07
### Fixed
- [Archetype] Set glue path by default ([#2975](https://github.com/cucumber/cucumber-jvm/pull/2975) M.P. Korstanje)

## [7.21.0] - 2025-02-02
### Added
- [Core] Pretty-Print DocStringArgument Step Arguments([#2953](https://github.com/cucumber/cucumber-jvm/pull/2953) Daniel Miladinov)
- [Core] Include hook type in cucumber message ([#2972](https://github.com/cucumber/cucumber-jvm/pull/2972) M.P. Korstanje)

### Changed
- [Archetype] Replace JUnit Jupiter with AssertJ ([#2969](https://github.com/cucumber/cucumber-jvm/pull/2969) M.P. Korstanje)
- [JUnit Platform Engine] Use JUnit Platform 1.11.3 (JUnit Jupiter 5.11.3)
- [Core] Update dependency io.cucumber:gherkin to v31.0.0
- [Core] Update dependency io.cucumber:messages to v27.2.0
- [Core] Update dependency io.cucumber:html-formatter to v21.9.0
- [Core] Update dependency io.cucumber:query to v13.2.0
- [Core] Update dependency io.cucumber:testng-xml-formatter to v0.3.1

### Fixed
- [Core] Include root cause when using DataTable.asList and friends ([#2949](https://github.com/cucumber/cucumber-jvm/pull/2949) M.P. Korstanje)
- [Core] Indent stacktrace in pretty formatter ([#2970](https://github.com/cucumber/cucumber-jvm/pull/2970) M.P. Korstanje)
- [JUnit Platform Engine] Set Engine-Version-cucumber attribute ([#2963](https://github.com/cucumber/cucumber-jvm/pull/2963) M.P. Korstanje)

## [7.20.1] - 2024-10-09
### Fixed
- [Core] Lazily start IncrementingUuidGenerator sessions([#2931](https://github.com/cucumber/cucumber-jvm/pull/2931) M.P. Korstanje)

## [7.20.0] - 2024-10-04
### Added
- [JUnit Platform Engine] Enable use of custom UUID generators ([#2926](https://github.com/cucumber/cucumber-jvm/pull/2926) M.P. Korstanje)
- [JUnit] Enable use of custom UUID generators ([#2926](https://github.com/cucumber/cucumber-jvm/pull/2926) M.P. Korstanje)
- [TestNG] Enable use of custom UUID generators ([#2926](https://github.com/cucumber/cucumber-jvm/pull/2926) M.P. Korstanje)

### Changed
- [JUnit Platform Engine] Use JUnit Platform 1.11.2 (JUnit Jupiter 5.11.2)

### Fixed
- [Core] Use custom UUID generators for hooks ([#2926](https://github.com/cucumber/cucumber-jvm/pull/2926) M.P. Korstanje)

## [7.19.0] - 2024-09-19
### Changed
- [JUnit Platform Engine] Use JUnit Platform 1.11.0 (JUnit Jupiter 5.11.0)

### Fixed
- [Spring] Document `@CucumberContextConfiguration` semantics ([#2887](https://github.com/cucumber/cucumber-jvm/pull/2887) M.P. Korstanje)
- [Core] Enhanced stack trace to include step location for better debugging in case of datatable conversion errors ([#2908](https://github.com/cucumber/cucumber-jvm/pull/2908) Thomas Deblock)
- [Archetype] Set `cucumber.junit-platform.naming-strategy` to `long` when using Surefire.

## [7.18.1] - 2024-07-18
### Changed
- [Core] Include parameterized scenario name in JUnit and TestNG XML report

### Fixed
- [Archetype] Use `import static` for Assertions in archetype ([#2899](https://github.com/cucumber/cucumber-jvm/issues/2798) Patrick Altaie)
- [Core] Escape json when writing html report ([#312](https://github.com/cucumber/html-formatter/pull/312] M.P. Korstanje)

## [7.18.0] - 2024-05-17
### Added
- [Core] The TeamCityPlugin for IntelliJ IDEA now uses the hook's method name for the name of the hook itself. ([#2798](https://github.com/cucumber/cucumber-jvm/issues/2798) V.V. Belov)
- [Core] Allow feature with line syntax to target rules and examples. ([#2884](https://github.com/cucumber/cucumber-jvm/issues/2884) M.P. Korstanje)

## [7.17.0] - 2024-04-18
### Added
- [JUnit Platform Engine] Support for parameters `cucumber.junit-platform.naming-strategy.short.example-name` and `cucumber.junit-platform.naming-strategy.long.example-name` ([#2743](https://github.com/cucumber/cucumber-jvm/issues/2743) V.V. Belov)

### Changed
- [Jakarta CDI] Update dependency jakarta.enterprise:jakarta.enterprise.cdi-api to v4.1.0
- [TestNG] Update dependency org.testng:testng to v7.10.1
- [Core] Use a [message based TestNG XML Formatter](https://github.com/cucumber/testng-xml-formatter) ([#2863](https://github.com/cucumber/cucumber-jvm/pull/2863) M.P. Korstanje)

## [7.16.1] - 2024-03-23
### Fixed
- [Core] Include stack traces in html report ([#2862](https://github.com/cucumber/cucumber-jvm/pull/2862) M.P. Korstanje)

## [7.16.0] - 2024-03-21
### Added
- [Core] Improved support for multiple classloaders in IncrementingUuidGenerator ([#2853](https://github.com/cucumber/cucumber-jvm/pull/2853) J. Kronegg)
- [Core] Assume numbers use either a comma or period for the thousands separator instead of non-breaking spaces. ([cucumber-expressions/#290](https://github.com/cucumber/cucumber-expressions/pull/290))
- [JUnit Platform Engine] Improve the cucumber.features warning ([#2856](https://github.com/cucumber/cucumber-expressions/pull/2856) M.P. Korstanje)
- [JUnit Platform Engine] Improve Maven and Gradle compatibility ([#2832](https://github.com/cucumber/cucumber-jvm/pull/2832) M.P. Korstanje)

### Changed
- [TestNG] Update dependency org.testng:testng to v7.9.0
- [Core] Update dependency io.cucumber:tag-expressions to v6.1.0
- [Core] Update Messages and dependants ([#2826](https://github.com/cucumber/cucumber-jvm/pull/2826))
- [Core] Update dependency io.cucumber:gherkin to v27.0.0
- [Core] Added Malayalam localization ([#2826](https://github.com/cucumber/cucumber-jvm/pull/2826))
- [Core] Added 'ed' to Italian ([gherkin/#31](https://github.com/cucumber/gherkin/issues/160))
- [Core] Added Danish translation of "Rule" ([#2826](https://github.com/cucumber/cucumber-jvm/pull/2826))
- [Core] Added Dutch translation of "Rule" ([#2826](https://github.com/cucumber/cucumber-jvm/pull/2826))
- [Core] Added Esperanto translation of "Rule" ([#2826](https://github.com/cucumber/cucumber-jvm/pull/2826))
- [JUnit Platform Engine] Use JUnit Platform 1.10.2 (JUnit Jupiter 5.10.2)
- [Core] Added Vietnamese translation of "Rule" ([gherkin/#204](https://github.com/cucumber/gherkin/pull/204))
- [Core] Added Irish translation of "Rule" ([gherkin/#216](https://github.com/cucumber/gherkin/pull/216))

### Fixed
- [Core] Missing execution steps statuses ([junit-xml-formatter/#24](https://github.com/cucumber/junit-xml-formatter/pull/24) F. Ahadi)
- [Core] Parse negative numbers in Norwegian (and 59 other languages) ([cucumber-expressions/#290](https://github.com/cucumber/cucumber-expressions/pull/290))

## [7.15.0] - 2023-12-11
### Added
- [Core] Support nested jar file systems (i.e. Spring Boot 3.2) ([#2830](https://github.com/cucumber/cucumber-jvm/pull/2830) M.P. Korstanje)

### Changed
- [Core] Upgrade `vis-timeline` to v7.7.3

## [7.14.1] - 2023-11-25
### Fixed
- [Guice] Inject static fields prior to before all hooks ([#2803](https://github.com/cucumber/cucumber-jvm/pull/2803) M.P. Korstanje)

## [7.14.0] - 2023-09-09
### Changed
- [Core] Update dependency io.cucumber:html-formatter to v20.4.0
- [Core] Download attachments that are not video, image or text from the html report ([react-components/#333](https://github.com/cucumber/react-components/pull/333) David J. Goss)

### Fixed
- [Core] Exclude Multi-Release files from Jackson while shading ([#2786](https://github.com/cucumber/cucumber-jvm/pull/2786) M.P. Korstanje)

## [7.13.0] - 2023-07-02
### Changed
- [TestNG] Update dependency org.testng:testng to v7.8.0

### Fixed
- [Pico] Fixed missing calls to start, stop and dispose handles ([#2772](https://github.com/cucumber/cucumber-jvm/pull/2772) Julien Kronegg)

## [7.12.1] - 2023-06-02
### Fixed
- [Core] Set html report viewport width to device width ([html-formatter/#238](https://github.com/cucumber/html-formatter/pull/238)  Tim Yao )
- [Core] Fixed `cucumber.publish.enabled=false` ([#2747](https://github.com/cucumber/cucumber-jvm/pull/2747) M.P. Korstanje)
- [JUnit Platform Engine] Fixed `cucumber.publish.enabled=false` ([#2747](https://github.com/cucumber/cucumber-jvm/pull/2747) M.P. Korstanje)
- [Java] Fixed duplicate step definition for classes with interfaces ([#2757](https://github.com/cucumber/cucumber-jvm/issues/2757) Julien Kronegg)
- [Pico] Fixed unsatisfiable dependency with disposables ([#2762](https://github.com/cucumber/cucumber-jvm/issues/2762) Julien Kronegg)

## [7.12.0] - 2023-04-29
### Added
- [JUnit Platform Engine] Add constant for fixed.max-pool-size property ([#2713](https://github.com/cucumber/cucumber-jvm/pull/2713) M.P. Korstanje)
- [Core] Support directories containing exclusively rerun files using the `@path/to/rerun` syntax ([#2710](https://github.com/cucumber/cucumber-jvm/pull/2710) Daniel Whitney, M.P. Korstanje)
- [Core] Improved event bus performance using UUID generator selectable through SPI ([#2703](https://github.com/cucumber/cucumber-jvm/pull/2703) Julien Kronegg)
- [Core] Added source reference in parameter type messages ([#2719](https://github.com/cucumber/cucumber-jvm/issues/2719) Julien Kronegg)
- [Core] Support for JetBrains Space ([ci-environment/#205](https://github.com/cucumber/ci-environment/pull/205) Viktor)

### Fixed
- [Pico] Improve performance ([#2724](https://github.com/cucumber/cucumber-jvm/issues/2724) Julien Kronegg)
- [JUnit 4] Fix swallowed exception ([#2714](https://github.com/cucumber/cucumber-jvm/issues/2714) M.P. Korstanje)
- [Guice] Fix NPE in Guice when configured incorrectly ([#2716](https://github.com/cucumber/cucumber-jvm/issues/2716) M.P. Korstanje)

## [7.11.2] - 2023-03-23
### Fixed
- [JUnit Platform Engine] Corrupted junit-xml report when using `surefire.rerunFailingTestsCount` parameter ([#2709](https://github.com/cucumber/cucumber-jvm/pull/2709) M.P. Korstanje)

## [7.11.1] - 2023-01-27
### Added
- [Core] Warn when `cucumber.options` is used ([#2685](https://github.com/cucumber/cucumber-jvm/pull/2685) M.P. Korstanje)

### Fixed
- [Spring] Instantiate `TestContextManager` synchronously ([#2686](https://github.com/cucumber/cucumber-jvm/pull/2686), [#2687](https://github.com/cucumber/cucumber-jvm/pull/2687) Thai Nguyen, M.P. Korstanje)

## [7.11.0] - 2023-01-12
### Added
- [Spring] Support Spring Boot 3 and Spring 6 ([#2644](https://github.com/cucumber/cucumber-jvm/pull/2644) M.P. Korstanje)
- [JUnit Platform] Support `cucumber.execution.parallel.config.config.fixed.max-pool-size` ([#2681](https://github.com/cucumber/cucumber-jvm/pull/2681) M.P. Korstanje)

### Changed
- [Core] Use a [message based JUnit XML Formatter](https://github.com/cucumber/junit-xml-formatter) ([#2638](https://github.com/cucumber/cucumber-jvm/pull/2638) M.P. Korstanje)
- [Core] Throw an exception when tag expressions are incorrectly escaped ([tag-expressions/#17](https://github.com/cucumber/tag-expressions/pull/17) Aslak Hellesøy)
- [DeltaSpike] Un-Deprecated deltaspike - can be made to work on Java 17 ([#2674](https://github.com/cucumber/cucumber-jvm/pull/2674) M.P. Korstanje)

### Fixed
- [Core] Improve test step creation performance ([#2666](https://github.com/cucumber/cucumber-jvm/issues/2666), Julien Kronegg)
- [JUnit Platform] Use JUnit Platform 1.9.2 (JUnit Jupiter 5.9.2)

## [7.10.1] - 2022-12-16
### Fixed
- [Spring] Inject CucumberContextConfiguration constructor dependencies ([#2664](https://github.com/cucumber/cucumber-jvm/pull/2664) M.P. Korstanje)

## [7.10.0] - 2022-12-11
### Added
- Enabled reproducible builds ([#2641](https://github.com/cucumber/cucumber-jvm/issues/2641) Hervé Boutemy )
- [Core] Mark Allure 5 and 6 plugins as incompatible ([#2652](https://github.com/cucumber/cucumber-jvm/issues/2652) M.P. Korstanje)
- [Spring] Invoke all `TestContextManager` methods ([#2661](https://github.com/cucumber/cucumber-jvm/pull/2661) M.P. Korstanje)

### Changed
- [TestNG] Update dependency org.testng:testng to v7.7.0

### Deprecated
- [DeltaSpike] Deprecated Deltaspike - does not work on Java 17.

### Fixed
- [Core] Emit exceptions on failure to handle test run finished events ([#2651](https://github.com/cucumber/cucumber-jvm/issues/2651) M.P. Korstanje)
- [Spring] @MockBean annotation not working with JUnit5 ([#2654](https://github.com/cucumber/cucumber-jvm/pull/2654) Alexander Kirilov, M.P. Korstanje)
- [Core] Improve expression creation performance ([cucumber-expressions/#187](https://github.com/cucumber/cucumber-expressions/pull/187), [cucumber-expressions/#189](https://github.com/cucumber/cucumber-expressions/pull/189), Julien Kronegg)

## [7.9.0] - 2022-11-01
### Added
- [Spring] Support @CucumberContextConfiguration as a meta-annotation ([#2491](https://github.com/cucumber/cucumber-jvm/issues/2491) Michael Schlatt)

### Changed
- [Core] Update dependency io.cucumber:gherkin to v25.0.2. Japanese Rule translation changed from Rule to ルール.
- [Core] Update dependency io.cucumber:gherkin to v24.1
- [Core] Delegate encoding and BOM handling to gherkin ([#2624](https://github.com/cucumber/cucumber-jvm/issues/2624) M.P. Korstanje)

### Fixed
- [Core] Don't swallow parse errors on the CLI ([#2632](https://github.com/cucumber/cucumber-jvm/issues/2632) M.P. Korstanje)

### Security
- [Core] Update dependency com.fasterxml.jackson to v2.13.4.20221012

## [7.8.1] - 2022-10-03
### Fixed
- [Core] Remove Jackson services from `META-INF/services` ([#2621](https://github.com/cucumber/cucumber-jvm/issues/2621) M.P. Korstanje)
- [JUnit Platform] Use JUnit Platform 1.9.1 (JUnit Jupiter 5.9.1)

## [7.8.0] - 2022-09-15
### Added
- [Core] Support comparison of expected and actual values in IntelliJ IDEA ([#2607](https://github.com/cucumber/cucumber-jvm/issues/2607) Andrey Vokin)
- [Core] Omit filtered out pickles from html report ([react-components/#273](https://github.com/cucumber/react-components/pull/273) David J. Goss)
- [Datatable] Support parsing Booleans in Datatables ([#2614](https://github.com/cucumber/cucumber-jvm/pull/2614) G. Jourdan-Weil)

## [7.7.0] - 2022-09-08
### Added
- [JUnit Platform] Enable parallel execution of features ([#2604](https://github.com/cucumber/cucumber-jvm/pull/2604) Sambathkumar Sekar)

## [7.6.0] - 2022-08-08
### Changed
- [Core] Update dependency io.cucumber:messages to v19
- [Core] Update dependency io.cucumber:gherkin to v24
- [Core] Update dependency io.cucumber:html-formatter to v20

## [7.5.0] - 2022-07-28
### Added
- [OpenEJB] Added new module `jakarta-openejb`, which supports the jakarta.* namespace in TomEE 9.x ([#2583](https://github.com/cucumber/cucumber-jvm/pull/2583) R. Zowalla)

### Changed
- [JUnit Platform] Use JUnit Platform 1.9.0 (JUnit Jupiter 5.9.0) ([#2590](https://github.com/cucumber/cucumber-jvm/pull/2590) M.P. Korstanje)
- [TestNG] Update dependency org.testng:testng to v7.6.1
- [Core] Update dependency io.cucumber:ci-environment to v9.1.0

### Fixed
- [Java] Process glue classes distinctly ([#2582](https://github.com/cucumber/cucumber-jvm/pull/2582) M.P. Korstanje)
- [Spring] Do not invoke after test methods if test failed to start ([#2585](https://github.com/cucumber/cucumber-jvm/pull/2585) M.P. Korstanje)

## [7.4.1] - 2022-06-23
### Fixed
- [Core] Fix NoSuchMethodError `PrintWriter(OutputStream, boolean, Charset)` ([#2578](https://github.com/cucumber/cucumber-jvm/pull/2578) M.P. Korstanje)

## [7.4.0] - 2022-06-22
### Added
- [Core] Warn when glue path is passed as file scheme instead of classpath ([#2547](https://github.com/cucumber/cucumber-jvm/pull/2547) M.P. Korstanje)

### Changed
- [Core] Flush pretty output manually ([#2573](https://github.com/cucumber/cucumber-jvm/pull/2573) M.P. Korstanje)

### Fixed
- [Spring] Cleanly stop after failure to start application context ([#2570](https://github.com/cucumber/cucumber-jvm/pull/2570) M.P. Korstanje)
- [JUnit] Scenario logging does not show up in step notifications ([#2563](https://github.com/cucumber/cucumber-jvm/pull/2545) M.P. Korstanje)

## [7.3.4] - 2022-05-02
### Fixed
- [Core] Fix problem with PrettyFormatter printing URL encoded strings ([#2545](https://github.com/cucumber/cucumber-jvm/pull/2545) skloessel)

## [7.3.3] - 2022-04-30
### Fixed
- [Core] Pretty print plugin performance issues; incorrect DataTable format in Gradle console ([#2541](https://github.com/cucumber/cucumber-jvm/pull/2541) Scott Davis)

## [7.3.2] - 2022-04-22
### Fixed
- [Core] Fix cucumber report spam `Collectors.toUnmodifiableList()` ([#2533](https://github.com/cucumber/cucumber-jvm/pull/2533) M.P. Korstanje)

## [7.3.1] - 2022-04-20
### Fixed
- [Core] Removed usage of since Java 10 `Collectors.toUnmodifiableList()` method ([#2531](https://github.com/cucumber/cucumber-jvm/pull/2531) M.P. Korstanje)

## [7.3.0] - 2022-04-19
### Added
- [JUnit Platform] Support `cucumber.features` property ([#2498](https://github.com/cucumber/cucumber-jvm/pull/2498) M.P. Korstanje)

### Changed
- [Core] Use null-safe messages ([#2497](https://github.com/cucumber/cucumber-jvm/pull/2497) M.P. Korstanje)
- Update dependency io.cucumber:messages to v18 ([#2497](https://github.com/cucumber/cucumber-jvm/pull/2497) M.P. Korstanje)
- Update dependency io.cucumber:gherkin to v23 ([#2497](https://github.com/cucumber/cucumber-jvm/pull/2497) M.P. Korstanje)
- Update dependency io.cucumber:ci-environment to v9 ([#2475](https://github.com/cucumber/cucumber-jvm/pull/2475) M.P. Korstanje)
- Update dependency com.google.inject:guice to v5.1.0 ([#2473](https://github.com/cucumber/cucumber-jvm/pull/2473) M.P. Korstanje)
- Update dependency org.testng:testng to v7.5 ([#2457](https://github.com/cucumber/cucumber-jvm/pull/2457) M.P. Korstanje)

### Fixed
- [OpenEJB] Remove spurious dependencies ([#2477](https://github.com/cucumber/cucumber-jvm/pull/2477) M.P. Korstanje)
- [TestNG] Remove spurious `Optional` from test name ([#2488](https://github.com/cucumber/cucumber-jvm/pull/2488) M.P. Korstanje)
- [BOM] Add missing dependencies to bill of materials ([#2496](https://github.com/cucumber/cucumber-jvm/pull/2496) M.P. Korstanje)
- [Spring] Start and stop test context once per scenario ([#2517](https://github.com/cucumber/cucumber-jvm/pull/2517) M.P. Korstanje)
- [JUnit Platform] Feature files with space in filename generate Illegal Character ([#2521](https://github.com/cucumber/cucumber-jvm/pull/2521) G. Fernandez)

## [7.2.3] - 2022-01-13
### Fixed
- [Core] Uncaught TypeError: e.git is undefined ([#2466](https://github.com/cucumber/cucumber-jvm/pull/2466) M.P. Korstanje)

## [7.2.2] - 2022-01-07
### Fixed
- [Core] Look up docstring converter by type as fallback ([#2459](https://github.com/cucumber/cucumber-jvm/pull/2459) M.P. Korstanje)

## [7.2.1] - 2022-01-04
### Fixed
- [Core] Fix NPE if git is not detected ([#2455](https://github.com/cucumber/cucumber-jvm/pull/2455) Aslak Hellesøy)

## [7.2.0] - 2022-01-03
### Added
- [Core] Support multiple doc strings types with same content type ([#2421](https://github.com/cucumber/cucumber-jvm/pull/2421) Postelnicu George)
- [Guice] Automatically detect `InjectorSource` ([#2432](https://github.com/cucumber/cucumber-jvm/pull/2432) Postelnicu George)
- [Core] Support proxy for publish plugin ([#2452](https://github.com/cucumber/cucumber-jvm/pull/2452) M.P. Korstanje)

### Changed
- [Core] Replaced `create-meta` dependency with `ci-environment` ([#2438](https://github.com/cucumber/cucumber-jvm/pull/2438) M.P. Korstanje)

### Deprecated
- [Guice] Deprecated `guice.injector-source` in favour of discovering `InjectorSource` ([#2432 ](https://github.com/cucumber/cucumber-jvm/pull/2432) M.P. Korstanje)

### Fixed
- [JUnit Platform] Delay plugin creation until test execution ([#2442](https://github.com/cucumber/cucumber-jvm/pull/2442) M.P. Korstanje)
- [Core] Display curl-like error message for more url output stream problems ([#2451](https://github.com/cucumber/cucumber-jvm/pull/2451) M.P. Korstanje)

## [7.1.0] - 2021-11-28
### Added
- [Core] Include `DefaultObjectFactory` as part of the API ([#2400](https://github.com/cucumber/cucumber-jvm/pull/2400) M.P. Korstanje)

### Changed
- [Core] Update dependency io.cucumber:tag-expressions to v4.1.0
- [Core] Support escape backslashes in tag expressions ([common/#1778](https://github.com/cucumber/common/pull/1778) Yusuke Noda)
- [JUnit Platform] Use JUnit Platform 1.8.2 (JUnit Jupiter 5.8.2)

### Deprecated
- [Core] Deprecated forgotten `TypeRegistry`.

## [7.0.0] - 2021-10-06

## [7.0.0-RC1] - 2021-09-11
### Added
- [Java] Added `@BeforeAll` and `@AfterAll` hooks ([cucumber/#1876](https://github.com/cucumber/cucumber-jvm/pull/1876) M.P. Korstanje)
- [JUnit Platform] Optionally use long names

### Changed
- [Core] Updated `cucumber-expressions` to v11 ([cucumber/#711](https://github.com/cucumber/cucumber/pull/771) M.P. Korstanje)
- [Core] Removed incorrect ISO 639-1 code for Telugu language ([cucumber/#1238](https://github.com/cucumber/cucumber/pull/1238) Nvmkpk)
- [Core] Deprecated the `Summary` plugin interface for removal.
- [Core] Removed the `default_summary` and `null_summary` plugins
- [Core] The `summary` plugin is enabled default when using the CLI. Use `--no-summary` to disable.
- [Core] The `progress` formatter is no longer enabled by default on CLI. Use `--plugin progress` to enable.
- [Core] Use transformer for all `DataTable.asX` methods. ([#2262](https://github.com/cucumber/cucumber-jvm/issues/2262) [cucumber/#1419](https://github.com/cucumber/cucumber/pull/1419) M.P. Korstanje)
- [TestNG] Automatically pick up properties from `testng.xml` ([#2354](https://github.com/cucumber/cucumber-jvm/pull/2354) M.P. Korstanje, Gayan Sandaruwan)
- [Core] Pretty formatter to print step DataTables ([#2330](https://github.com/cucumber/cucumber-jvm/pull/2330) Arty Sidorenko)
- [Core] `Scenario.getId()` returns the actual scenario id ([#2366](https://github.com/cucumber/cucumber-jvm/issues/2366) M.P. Korstanje)

### Deprecated
- [JUnit Platform] Deprecated `@Cucumber` in favour of `@Suite` ([#2362](https://github.com/cucumber/cucumber-jvm/pull/2362) M.P. Korstanje)

### Fixed
- [Core] Emit step hook messages ([#2009](https://github.com/cucumber/cucumber-jvm/issues/2093) Grasshopper)
- [Core] Synchronize event bus before use ([#2358](https://github.com/cucumber/cucumber-jvm/pull/2358)) M.P. Korstanje)

### Removed
- [Core] Removed `--strict` and `--no-strict` options ([#1788](https://github.com/cucumber/cucumber-jvm/issues/1788) M.P. Korstanje)
- [Core]  Cucumber executes scenarios in strict mode by default ([#1788](https://github.com/cucumber/cucumber-jvm/issues/1788) M.P. Korstanje)
- [Core] Removed deprecated `TypeRegistryConfigurer`. Use `@ParameterType` instead. ([#2356](https://github.com/cucumber/cucumber-jvm/issues/2356) M.P. Korstanje)
- [Weld] Removed `cucumber-weld` in favour of `cucumber-jakarta-cdi` or `cucumber-cdi2`. ([#2276](https://github.com/cucumber/cucumber-jvm/issues/2276) M.P. Korstanje)
- [Needle] Removed `cucumber-needled` in favour of `cucumber-jakarta-cdi` or `cucumber-cdi2`. ([#2276](https://github.com/cucumber/cucumber-jvm/issues/2276) M.P. Korstanje)

[Unreleased]: https://github.com/cucumber/cucumber-jvm/compare/v7.25.0...HEAD
[7.25.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.24.0...v7.25.0
[7.24.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.23.0...v7.24.0
[7.23.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.22.2...v7.23.0
[7.22.2]: https://github.com/cucumber/cucumber-jvm/compare/v7.22.1...v7.22.2
[7.22.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.22.0...v7.22.1
[7.22.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.21.1...v7.22.0
[7.21.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.21.0...v7.21.1
[7.21.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.20.1...v7.21.0
[7.20.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.20.0...v7.20.1
[7.20.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.19.0...v7.20.0
[7.19.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.18.1...v7.19.0
[7.18.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.18.0...v7.18.1
[7.18.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.17.0...v7.18.0
[7.17.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.16.1...v7.17.0
[7.16.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.16.0...v7.16.1
[7.16.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.15.0...v7.16.0
[7.15.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.14.1...v7.15.0
[7.14.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.14.0...v7.14.1
[7.14.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.13.0...v7.14.0
[7.13.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.12.1...v7.13.0
[7.12.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.12.0...v7.12.1
[7.12.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.11.2...v7.12.0
[7.11.2]: https://github.com/cucumber/cucumber-jvm/compare/v7.11.1...v7.11.2
[7.11.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.11.0...v7.11.1
[7.11.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.10.1...v7.11.0
[7.10.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.10.0...v7.10.1
[7.10.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.9.0...v7.10.0
[7.9.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.8.1...v7.9.0
[7.8.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.8.0...7.8.1
[7.8.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.7.0...v7.8.0
[7.7.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.6.0...v7.7.0
[7.6.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.5.0...v7.6.0
[7.5.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.4.1...v7.5.0
[7.4.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.4.0...v7.4.1
[7.4.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.3.4...v7.4.0
[7.3.4]: https://github.com/cucumber/cucumber-jvm/compare/v7.3.3...v7.3.4
[7.3.3]: https://github.com/cucumber/cucumber-jvm/compare/v7.3.2...v7.3.3
[7.3.2]: https://github.com/cucumber/cucumber-jvm/compare/v7.3.1...v7.3.2
[7.3.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.3.0...v7.3.1
[7.3.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.2.3...v7.3.0
[7.2.3]: https://github.com/cucumber/cucumber-jvm/compare/v7.2.2...v7.2.3
[7.2.2]: https://github.com/cucumber/cucumber-jvm/compare/v7.2.1...v7.2.2
[7.2.1]: https://github.com/cucumber/cucumber-jvm/compare/v7.2.0...v7.2.1
[7.2.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.1.0...v7.2.0
[7.1.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.0.0...v7.1.0
[7.0.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.0.0-RC1...v7.0.0
[7.0.0-RC1]: https://github.com/cucumber/cucumber-jvm/compare/v6.11.0...v7.0.0-RC1
