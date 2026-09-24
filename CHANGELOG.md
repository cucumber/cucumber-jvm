# Changelog

All notable changes to the current version this project will be documented in
this file. For previous versions see the [release-notes archive](release-notes).

For migration instructions from previous major version and a long form
explanation of noteworthy changes see the [Release Announcement](release-notes/v8.0.0.md).

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [8.0.0] - 2026-09-24
### Added
- [Java] Add optional names to `@Before`, `@After`, `@BeforeStep` and `@AfterStep` hooks and emit hook names in messages ([#2917](https://github.com/cucumber/cucumber-jvm/issues/2917), [#3173](https://github.com/cucumber/cucumber-jvm/pull/3173))
- [Java] Declare step definitions with minimal ceremony ([#3200](https://github.com/cucumber/cucumber-jvm/issues/3200))
- [Java] Display hints when the glue is not efficiently configured ([#3151](https://github.com/cucumber/cucumber-jvm/pull/3151), Julien Kronegg)
- [JUnit Platform] Use `ParallelHierarchicalTestExecutorServiceFactory` ([#3105](https://github.com/cucumber/cucumber-jvm/pull/3105))
- [Core] Support both DocString and DataTable arguments on steps ([#3213](https://github.com/cucumber/cucumber-jvm/pull/3213))
- [Core] Support `cucumber.execution.threads` property when executing from CLI ([#3183](https://github.com/cucumber/cucumber-jvm/pull/3183))
- [Core] Support registering individual glue classes via `cucumber.glue.classes` property or the `--glue-classes` CLI option ([#3120](https://github.com/cucumber/cucumber-jvm/pull/3120)).
- [Core] Support class filtering before class loading via the `{included,excluded}-class-name-pattern` property and the `--glue-{included,excluded}-class-name-pattern`  CLI option. ([#3120](https://github.com/cucumber/cucumber-jvm/pull/3120)).
- [Core] Emit messages for `@BeforeAll` and `@AfterAll` hooks ([#3236](https://github.com/cucumber/cucumber-jvm/pull/3236))

### Changed
- [Core] Jackson is now an opt-in dependency ([#3206](https://github.com/cucumber/cucumber-jvm/pull/3206))
- [All] Set baseline to Java 17 ([#3116](https://github.com/cucumber/cucumber-jvm/pull/3116))
- [TestNG] Provide scenarios during dry-run ([#3234](https://github.com/cucumber/cucumber-jvm/pull/3234))
- [All] Adopt [JSpecify](https://jspecify.dev/) to declare nullability ([#3116](https://github.com/cucumber/cucumber-jvm/pull/3116))
- [Core] Update skipped, pending and undefined colors ([cucumber/common#2302](https://github.com/cucumber/common/issues/2302))
- [Core] Update dependency io.cucumber:ci-environment.version to v15.0.0
- [Core] Update dependency io.cucumber:cucumber-expressions.version to v20.1.0
- [Core] Update dependency io.cucumber:cucumber-json-formatter.version to v0.4.1
- [Core] Update dependency io.cucumber:gherkin.version to v42.0.1
- [Core] Update dependency io.cucumber:html-formatter.version to v24.1.0
- [Core] Update dependency io.cucumber:junit-xml-formatter.version to v0.15.0
- [Core] Update dependency io.cucumber:messages.version to v34.2.1
- [Core] Update dependency io.cucumber:messages-ndjson.version to v0.5.2
- [Core] Update dependency io.cucumber:pretty-formatter.version to v4.0.2
- [Core] Update dependency io.cucumber:query.version to v16.1.1
- [Core] Update dependency io.cucumber:tag-expressions.version to v10.0.1
- [Core] Update dependency io.cucumber:teamcity-formatter.version to v0.3.0
- [Core] Update dependency io.cucumber:testng-xml-formatter.version to v0.9.1
- [Core] Update dependency io.cucumber:usage-formatter.version to v0.2.0
- [JUnit Platform Engine] Use JUnit Platform 6.1.2 (JUnit Jupiter 6.1.2) ([#3162](https://github.com/cucumber/cucumber-jvm/pull/3162))
- [All] Classes not designed for extension are now final. See [api-changes.json](./.revapi/api-changes.json) for details.
- [All] Utility classes are no longer instantiatable. See [api-changes.json](./.revapi/api-changes.json) for details.

### Deprecated
- [JUnit] Deprecate `cucumber-testng` for removal in favor of `cucumber-junit-platform-engine` ([#3245](https://github.com/cucumber/cucumber-jvm/pull/3245))
- [JUnit] Deprecate `cucumber-junit` for removal in favor of `cucumber-junit-platform-engine`

### Fixed
- [Core] Report the source location of both definitions when a parameter type is registered twice with the same name ([#3144](https://github.com/cucumber/cucumber-jvm/issues/3144))
- [JUnit Platform Engine] Accept partial matches with `cucumber.filter.name` and align the behavior with JUnit 4 and CLI ([#3174](https://github.com/cucumber/cucumber-jvm/pull/3174))
- [JUnit Platform Engine] Don't require global read lock ([#3103](https://github.com/cucumber/cucumber-jvm/pull/3103))
- [Core] Don't swallow exceptions thrown by `Plugin` ([#3236](https://github.com/cucumber/cucumber-jvm/pull/3236))

### Removed
- [OpenEJB] Removed `cucumber-openejb` in favor of `cucumber-jakarta-openejb` ([#3189](https://github.com/cucumber/cucumber-jvm/pull/3189))
- [OpenEJB] Removed `cucumber-cdi2` in favor of `cucumber-jakarta-cdi` ([#3192](https://github.com/cucumber/cucumber-jvm/pull/3192))
- [OpenEJB] Removed `cucumber-deltaspike` without replacement ([#3193](https://github.com/cucumber/cucumber-jvm/pull/3193))

[Unreleased]: https://github.com/cucumber/cucumber-jvm/compare/v8.0.0...HEAD
[8.0.0]: https://github.com/cucumber/cucumber-jvm/compare/v7.34.9...v8.0.0
