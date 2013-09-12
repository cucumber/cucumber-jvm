## [1-1-5-SNAPSHOT (Git master)](https://github.com/cucumber/cucumber-jvm/compare/v1.1.4...master)

* [Core] Step Definition and Hook timeout is now a `long` instead of an `int`. (Aslak Hellesøy)
* [Rhino] Before and After hooks support ([#587](https://github.com/cucumber/cucumber-jvm/pull/587) Rui Figueira)
* [Android] Separate CI job for Android. ([#581](https://github.com/cucumber/cucumber-jvm/issues/581), [#584](https://github.com/cucumber/cucumber-jvm/pull/584) Björn Rasmusson)
* [Android] Add support for Dependency Injection via cucumber-picocontainer, cucumber-guice, cucumber-spring etx. (Aslak Hellesøy)
* [TestNG] Java Calculator TestNG example project ([#579](https://github.com/cucumber/cucumber-jvm/pull/579) Dmytro Chyzhykov)
* [Jython] Access to scenario in Before and After hooks ([#582](https://github.com/cucumber/cucumber-jvm/issues/582) Aslak Hellesøy)
* [Core] Replace placeholders in the Scenario Outline title ([#580](https://github.com/cucumber/cucumber-jvm/pull/580), [#510](https://github.com/cucumber/cucumber-jvm/issues/510) Jamie W. Astin)
* [JUnit/Core] `@cucumber.junit.api.Cucumber.Options` is deprecated in favour of `@cucumber.api.Options` ([#549](https://github.com/cucumber/cucumber-jvm/issues/549) Aslak Hellesøy)
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
* [Android] Deploy a jar for cucumber-android. ([#573](https://github.com/cucumber/cucumber-jvm/issues/573) Maximilian Fellner)

## [1.1.4](https://github.com/cucumber/cucumber-jvm/compare/v1.1.3...v1.1.4) (2013-08-11)

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

## [1.1.3](https://github.com/cucumber/cucumber-jvm/compare/v1.1.2...v1.1.3) (2013-03-10)

* [Core] Added accessors to `TableDiffException`. ([#384](https://github.com/cucumber/cucumber-jvm/issues/384) Aslak Hellesøy)
* [Core] Fixed use of formatter to list all step results in JSON output ([#426](https://github.com/cucumber/cucumber-jvm/pull/426) agattiker)
* [Scala] Add support for DataTable and locale-aware type transformations. ([#443](https://github.com/cucumber/cucumber-jvm/issues/443), [#455](https://github.com/cucumber/cucumber-jvm/pull/455) Matthew Lucas)
* [Groovy] Groovy should throw exception if more then one World registred ([#464](https://github.com/cucumber/cucumber-jvm/pull/464), [#458](https://github.com/cucumber/cucumber-jvm/issues/458) Luxor)
* [Core] Diffing tables doesn't work when delta span multiple lines ([#465](https://github.com/cucumber/cucumber-jvm/pull/465) Gilles Philippart)
* [JRuby] `GEM_PATH` and `RUBY_VERSION` can be set in env var, system property or `cucumber-jruby.properties` resource bundle. (Aslak Hellesøy)
* [JRuby] Wrong CompatVersion passed to JRuby when 1.9 is given ([#415](https://github.com/cucumber/cucumber-jvm/issues/415) David Kowis)
* [Core] Custom Formatter/Reporter's `before` and `after` hook weren't run. (Aslak Hellesøy)
* [Clojure] Clojure backend should define HookDefinition.getLocation(boolean detail) ([#461](https://github.com/cucumber/cucumber-jvm/issues/461), [#471](https://github.com/cucumber/cucumber-jvm/pull/471) Nils Wloka)

## [1.1.2](https://github.com/cucumber/cucumber-jvm/compare/v1.1.1...v1.1.2) (2013-01-30)

* [Core] Restore ability to diff with another DataTable ([#413](https://github.com/cucumber/cucumber-jvm/pull/413) Gilles Philippart)
* [Core] Executing a test with the --dry-run option does not skip the @Before or @After annotations ([#424](https://github.com/cucumber/cucumber-jvm/issues/424), [#444](https://github.com/cucumber/cucumber-jvm/pull/444) William Powell)
* [Clojure] Updated lein-cucumber version to 1.0.1 ([#414](https://github.com/cucumber/cucumber-jvm/pull/414) Nils Wloka)
* [JUnit] Upgrade to 4.11 ([#322](https://github.com/cucumber/cucumber-jvm/issues/322) [#445](https://github.com/cucumber/cucumber-jvm/pull/445) Petter Måhlén, Aslak Hellesøy)
* [Spring] Upgrade to 3.2.1.RELEASE (Aslak Hellesøy)
* [Core] Strip command line arguments in case people accidentally invoke `cucumber.api.cli.Main` with arguments that have spaces left and right. (Aslak Hellesøy)
* [Core] Implemented `DataTable.equals()` and `DataTable.hashCode()`. (Aslak Hellesøy)
* [Core] Support `DataTable.toTable(List<String[]>) and `DataTable.toTable(List<Map<String,String>>)` ([#433](https://github.com/cucumber/cucumber-jvm/issues/433), [#434](https://github.com/cucumber/cucumber-jvm/pull/434) Nicholas Albion, Aslak Hellesøy)
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

## [1.1.1](https://github.com/cucumber/cucumber-jvm/compare/v1.0.14...1.1.1) (2012-10-25)

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

## [1.0.14](https://github.com/cucumber/cucumber-jvm/compare/v1.0.12...v1.0.14) (2012-08-20)

(The 1.0.13 release failed half way through)

* [Core] gherkin.jar, gherkin-jvm-deps.jar and cucumber-jvm-deps.jar are embedded inside cucumber-core.jar (to simplify installation) (Aslak Hellesøy)

## [1.0.12](https://github.com/cucumber/cucumber-jvm/compare/v1.0.11...v1.0.12) (2012-08-19)

* [Core] No img data in embeddings using both json and html reports ([#339](https://github.com/cucumber/cucumber-jvm/issues/339) Aslak Hellesøy)
* [Core] JUnit assume failures (`AssumptionViolatedException`) behaves in the same way as pending (`cucumber.runtime.PendingException`) ([#359](https://github.com/cucumber/cucumber-jvm/issues/359) Aslak Hellesøy, Kim Saabye Pedersen)
* [Core] Extend url protocols. This makes it possible to load features and glue from within a container such as Arquilian. ([#360](https://github.com/cucumber/cucumber-jvm/issues/360), [#361](https://github.com/cucumber/cucumber-jvm/pull/361) Logan McGrath)
* [Jython] Jython Before/After Annotations ([#362](https://github.com/cucumber/cucumber-jvm/pull/362) Stephen Abrams)
* [Java] Support for delimited lists in step parameters ([#364](https://github.com/cucumber/cucumber-jvm/issues/364), [#371](https://github.com/cucumber/cucumber-jvm/pull/371) Marquis Wang)
* [Groovy] Load `env.groovy` before other glue code files. ([#374](https://github.com/cucumber/cucumber-jvm/pull/374) Tomas Bezdek)
* [Clojure] Add utilities for reading tables ([#376](https://github.com/cucumber/cucumber-jvm/pull/376) rplevy-draker)

## [1.0.11](https://github.com/cucumber/cucumber-jvm/compare/v1.0.10...v1.0.11) (2012-07-06)

* [Core] Added a new `@Transform` annotation and an abstract `Transformer` class giving full control over argument transforms.
* [OpenEJB] Remove log4j need for openejb module ([#355](https://github.com/cucumber/cucumber-jvm/pull/355) rmannibucau)
* [JUnit] JUnit report doesn't correctly report errors ([#315](https://github.com/cucumber/cucumber-jvm/issues/315), [#356](https://github.com/cucumber/cucumber-jvm/pull/356) Kevin Cunningham)

## [1.0.10](https://github.com/cucumber/cucumber-jvm/compare/v1.0.9...v1.0.10) (2012-06-20)

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

## [1.0.9](https://github.com/cucumber/cucumber-jvm/compare/v1.0.8...v1.0.9) (2012-06-08)

* [Core] Exceptions thrown from a step definition are no longer wrapped in CucumberException. (Aslak Hellesøy)
* [Core] Fixed regression: PendingException was causing steps to fail instead of pending. ([#328](https://github.com/cucumber/cucumber-jvm/issues/328) Aslak Hellesøy)
* [Java] Missing String.format parameters in DefaultJavaObjectFactory ([#336](https://github.com/cucumber/cucumber-jvm/issues/336) paulkrause88, Aslak Hellesøy)
* [Core] Exceptions being swallowed if reported in a Hook ([#133](https://github.com/cucumber/cucumber-jvm/issues/133) David Kowis, Aslak Hellesøy)
* [Core] Added `DataTable.asMaps()` and made all returned lists immutable. (Aslak Hellesøy).
* [Java] The java-helloworld example has a simple example illustrating data tables and doc strings. (Aslak Hellesøy).
* [Core] Run scenarios/features by name ([#233](https://github.com/cucumber/cucumber-jvm/issues/233), [#323](https://github.com/cucumber/cucumber-jvm/pull/323) Klaus Bayrhammer)
* [Jython] Added missing `self` argument in Jython snippets. ([#324](https://github.com/cucumber/cucumber-jvm/issues/324) Aslak Hellesøy)
* [Scala] Fixed regression from v1.0.6 in Scala module - glue code wasn't loaded at all. ([#321](https://github.com/cucumber/cucumber-jvm/issues/321) Aslak Hellesøy)

## [1.0.8](https://github.com/cucumber/cucumber-jvm/compare/v1.0.7...v1.0.8) (2012-05-17)

* [Core] Ability to create `DataTable` objects from a List of objects while specifying what header columns (fields) to use (Aslak Hellesøy)
* [Core] `table.diff(listOfPojos)` no longer spuriously fails because of pseudo-random column/field ordering (Aslak Hellesøy)
* [Core] Tables with empty cells make the column disappear ([#320](https://github.com/cucumber/cucumber-jvm/pull/320) Aslak Hellesøy, Gilles Philippart)
* [Java] Add 'throws Throwable' to generated Java stepdef snippets ([#318](https://github.com/cucumber/cucumber-jvm/issues/318), [#319](https://github.com/cucumber/cucumber-jvm/pull/319) Petter Måhlén)
* [Core] Remove forced UTC timezone. ([#317](https://github.com/cucumber/cucumber-jvm/pull/317) Gilles Philippart)
* [Core] Options (Command line or `@Cucumber.Options`) can be overriden with the `cucumber.options` system property. (Aslak Hellesøy)

## [1.0.7](https://github.com/cucumber/cucumber-jvm/compare/v1.0.6...v1.0.7) (2012-05-10)

* [Java] cucumber-java lazily creates instances, just like the other DI containers. (Aslak Hellesøy)
* [Core] Throw an exception if a glue or feature path doesn't exist (i.e. neither file nor directory) (Aslak Hellesøy)

## [1.0.6](https://github.com/cucumber/cucumber-jvm/compare/v1.0.4...v1.0.6) (2012-05-03)

* [JUnit] Scenarios with skipped, pending or undefined steps show up as yellow in IDEA and Eclipse (They used to be green while the steps were yellow). (Aslak Hellesøy)
* [Core] Loading features and glue code from the `CLASSPATH` can be done with `classpath:my/path` ([#312](https://github.com/cucumber/cucumber-jvm/issues/312) Aslak Hellesøy)
* [Clojure] Clojure example can't find cuke_steps.clj ([#291](https://github.com/cucumber/cucumber-jvm/issues/291), [#309](https://github.com/cucumber/cucumber-jvm/pull/309) Nils Wloka)

## [1.0.4](https://github.com/cucumber/cucumber-jvm/compare/v1.0.3...v1.0.4) (2012-04-23)

* [Core] Ability to specify line numbers: `@Cucumber.Options(features = "my/nice.feature:2:10")` ([#234](https://github.com/cucumber/cucumber-jvm/issues/234) Aslak Hellesøy)
* [WebDriver] Improved example that shows how to reuse a driver for the entire JVM. (Aslak Hellesøy)
* [Core] Allow custom @XStreamConverter to be used on regular arguments - not just table arguments. (Aslak Hellesøy)
* [Groovy] fixed & simplified groovy step snippets ([#303](https://github.com/cucumber/cucumber-jvm/pull/303) Martin Hauner)
* [Java] Detect subclassing in glue code and report to the user that it's illegal. ([#301](https://github.com/cucumber/cucumber-jvm/issues/301) Aslak Hellesøy)
* [Core] Friendlier error message when XStream fails to assign null to primitive fields ([#296](https://github.com/cucumber/cucumber-jvm/issues/296) Aslak Hellesøy)

## [1.0.3](https://github.com/cucumber/cucumber-jvm/compare/v1.0.2...v1.0.3) (2012-04-19)

* [Core] Friendlier error message when XStream fails conversion ([#296](https://github.com/cucumber/cucumber-jvm/issues/296) Aslak Hellesøy)
* [Core] Empty strings from matched steps and table cells are converted to `null`. This means boxed types must be used if you intend to have empty strings. (Aslak Hellesøy)
* [Core] Implement --strict ([#196](https://github.com/cucumber/cucumber-jvm/issues/196), [#284](https://github.com/cucumber/cucumber-jvm/pull/284) Klaus Bayrhammer)
* [Clojure] Cucumber-clojure adding after hook to before ([#294](https://github.com/cucumber/cucumber-jvm/pull/294) Daniel E. Renfer)
* [Java] Show code source for Java step definitions in case of duplicates or ambiguous stepdefs. (Aslak Hellesøy).
* [Groovy] Arity mismatch can be avoided by explicitly declaring an empty list of closure parameters. ([#297](https://github.com/cucumber/cucumber-jvm/issues/297) Aslak Hellesøy)
* [Core] Added DataTable.toTable(List<?> other) for creating a new table. Handy for printing a table when diffing isn't helpful. (Aslak Hellesøy)

## [1.0.2](https://github.com/cucumber/cucumber-jvm/compare/v1.0.1...v1.0.2) (2012-04-03)

* [Java] Snippets using a table have a hint about how to use List<YourClass>. (Aslak Hellesøy)
* [Java] Don't convert paths to package names - instead throw an exception. This helps people avoid mistakes. (Aslak Hellesøy)
* [Scala] Fixed generated Scala snippets ([#282](https://github.com/cucumber/cucumber-jvm/pull/282) pawel-s)
* [JUnit] Automatically turn off ANSI colours when launched from IDEA. (Aslak Hellesøy)

## [1.0.1](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0...v1.0.1) (2012-03-29)

* [Clojure] Fix quoting of generated Clojure snippets ([#277](https://github.com/cucumber/cucumber-jvm/pull/277) Michael van Acken)
* [Guice] Guice in multi module/class loader setup ([#278](https://github.com/cucumber/cucumber-jvm/pull/278) Matt Nathan)
* [JUnit] Background steps show up correctly in IntelliJ ([#276](https://github.com/cucumber/cucumber-jvm/issues/276) Aslak Hellesøy)

## [1.0.0](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC24...v1.0.0) (2012-03-27)

* [Docs] Added Cuke4Duke migration notes to README ([#239](https://github.com/cucumber/cucumber-jvm/pull/239) coldbloodedtx)
* [Core] Added --monochrome flag, allowing monochrome output for certain formatters ([#221](https://github.com/cucumber/cucumber-jvm/issues/221) Aslak Hellesøy)
* [Core] Added a usage formatter ([#207](https://github.com/cucumber/cucumber-jvm/issues/207), [#214](https://github.com/cucumber/cucumber-jvm/pull/214) Klaus Bayrhammer)
* [Core] JavaScript-Error in HTML-Report when using ScenarioResult.write ([#254](https://github.com/cucumber/cucumber-jvm/issues/254) Aslak Hellesøy)
* [Java] Add support for enums in stepdefs ([#217](https://github.com/cucumber/cucumber-jvm/issues/217), [#240](https://github.com/cucumber/cucumber-jvm/pull/240) Gilles Philippart)
* [Core] Help text for CLI. ([#142](https://github.com/cucumber/cucumber-jvm/issues/142) Aslak Hellesøy)
* [JUnit] Eclipse JUnit reports inaccurate run count ([#263](https://github.com/cucumber/cucumber-jvm/issues/263), [#274](https://github.com/cucumber/cucumber-jvm/pull/274) dgradl)

## [1.0.0.RC24](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC23...v1.0.0.RC24) (2012-03-22)

* [Core] Understandable error message if a formatter needs output location. ([#148](https://github.com/cucumber/cucumber-jvm/issues/148), [#232](https://github.com/cucumber/cucumber-jvm/issues/232), [#269](https://github.com/cucumber/cucumber-jvm/issues/269) Aslak Hellesøy)
* [JUnit] Running with JUnit uses a null formatter by default (instead of a progress formatter). (Aslak Hellesøy)
* [Clojure] Fix release artifacts so cucumber-clojure can be released. ([#270](https://github.com/cucumber/cucumber-jvm/issues/270) Aslak Hellesøy)
* [Java] The @Pending annotation no longer exists. Throw a PendingException instead ([#271](https://github.com/cucumber/cucumber-jvm/issues/271) Aslak Hellesøy)

## [1.0.0.RC23](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC22...v1.0.0.RC23) (2012-03-20)

* [JUnit] CucumberException when running Cucumber with Jacoco code coverage ([#258](https://github.com/cucumber/cucumber-jvm/issues/258) Jan Stamer, Aslak Hellesøy)
* [Scala] Scala Javadoc problems with build ([#231](https://github.com/cucumber/cucumber-jvm/issues/231) Aslak Hellesøy)

## [1.0.0.RC22](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC21...v1.0.0.RC22) (2012-03-20)

* [Java] Snippets for DataTable include a hint about using List<YourType>, so people discover this neat technique (Aslak Hellesøy)
* [Core] Support DocString and DataTable in generated snippets ([#227](https://github.com/cucumber/cucumber-jvm/issues/227) Aslak Hellesøy)
* [Core] Fix broken --tags option (and get rid of JCommander for CLI parsing). ([#266](https://github.com/cucumber/cucumber-jvm/issues/266) Aslak Hellesøy)
* [Clojure] Make Clojure DSL syntax cleaner ([#244](https://github.com/cucumber/cucumber-jvm/issues/244) [#267](https://github.com/cucumber/cucumber-jvm/pull/267) rplevy-draker)
* [Clojure] Native Clojure backend ([#138](https://github.com/cucumber/cucumber-jvm/pull/138) [#265](https://github.com/cucumber/cucumber-jvm/pull/265) Kevin Downey, Nils Wloka)
* [JUnit] Added `format` attribute to `@Cucumber.Options` (Aslak Hellesøy)

## [1.0.0.RC21](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC20...v1.0.0.RC21) (2012-03-18)

* [Core] Ignore duplicate features instead of throwing exception. ([#259](https://github.com/cucumber/cucumber-jvm/issues/259) Aslak Hellesøy)
* [Core] Wrong message when runner on a non existing tag on feature ([#245](https://github.com/cucumber/cucumber-jvm/issues/245) Aslak Hellesøy, Jérémy Goupil)
* [Groovy, JRuby, Rhino] Make sure UTF-8 encoding is used everywhere ([#251](https://github.com/cucumber/cucumber-jvm/issues/251) Aslak Hellesøy)
* [Core, Cloure] Fixed StepDefinitionMatch to work with StepDefinitions that return null for getParameterTypes ([#250](https://github.com/cucumber/cucumber-jvm/issues/250), [#255](https://github.com/cucumber/cucumber-jvm/pull/255) Nils Wloka)
* [Java] Open up the `JavaBackend` API to ease integration from other tools ([#257](https://github.com/cucumber/cucumber-jvm/pull/257) Aslak Hellesøy).
* [Java] Inheritance in glue classes (stepdefs and hooks) is no longer supported - it causes too many problems. (Aslak Hellesøy).
* [JUnit] `@Cucumber.Options` annotation replaces `@Feature` annotation ([#160](https://github.com/cucumber/cucumber-jvm/issues/160) Aslak Hellesøy)
* [Spring] Slow Spring context performance ([#241](https://github.com/cucumber/cucumber-jvm/issues/241), [#242](https://github.com/cucumber/cucumber-jvm/pull/242) Vladimir Klyushnikov)
* [Core] Support for java.util.Calendar arguments in stepdefs. (Aslak Hellesøy)

## [1.0.0.RC20](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC16...v1.0.0.RC20) (2012-02-29)

* [JUnit] Improved JUnit runner. ([#107](https://github.com/cucumber/cucumber-jvm/issues/107), [#211](https://github.com/cucumber/cucumber-jvm/issues/211), [#216](https://github.com/cucumber/cucumber-jvm/pull/216) Giso Deutschmann)
* [Core] Stacktrace filtering filters away too much. ([#228](https://github.com/cucumber/cucumber-jvm/issues/228) Aslak Hellesøy)
* [Groovy] Fix native Groovy cucumber CLI ([#212](https://github.com/cucumber/cucumber-jvm/issues/212) Martin Hauner)
* [Core] Indeterministic feature ordering on Unix ([#224](https://github.com/cucumber/cucumber-jvm/issues/224) hutchy2570)
* [JUnit] New JUnitFormatter (--format junit) that outputs Ant-style JUnit XML. ([#226](https://github.com/cucumber/cucumber-jvm/pull/226), [#171](https://github.com/cucumber/cucumber-jvm/issues/171) Vladimir Miguro)

## [1.0.0.RC16](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC15...v1.0.0.RC16) (2012-02-20)

* [Core] Embed text and images in reports. ([#205](https://github.com/cucumber/cucumber-jvm/issues/205) Aslak Hellesøy)
* [Core] Detect duplicate step definitions. (Aslak Hellesøy)
* [Java] Auto-generated step definitions should escape dollar signs / other regex chars ([#204](https://github.com/cucumber/cucumber-jvm/issues/204), [#215](https://github.com/cucumber/cucumber-jvm/pull/215) Ian Dees)
* [Core] Scenario Outlines work with tagged hooks. ([#209](https://github.com/cucumber/cucumber-jvm/issues/209), [#210](https://github.com/cucumber/cucumber-jvm/issues/210) Aslak Hellesøy)
* [Spring] Allowed customization of Spring step definitions context ([#203](https://github.com/cucumber/cucumber-jvm/pull/203) Vladimir Klyushnikov)
* [Core] Ambiguous step definitions don't cause Cucumber to blow up, they just fail the step. (Aslak Hellesøy)
* [Java] Fixed NullPointerException in ClasspathMethodScanner ([#201](https://github.com/cucumber/cucumber-jvm/pull/201) Vladimir Klyushnikov)
* [Groovy] Compiled Groovy stepdef scripts are found as well as source ones (Aslak Hellesøy)
* [Jython] I18n translations for most languages. Languages that can't be transformed to ASCII are excluded. ([#176](https://github.com/cucumber/cucumber-jvm/issues/176), [#197](https://github.com/cucumber/cucumber-jvm/pull/197) Stephen Abrams)

## [1.0.0.RC15](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC14...v1.0.0.RC15) (2012-02-07)

* [Java] You must use `cucumber.runtime.xstream` instead of `com.thoughtworks.xstream` for custom converters.
* [Core] XStream and Diffutils are now packaged inside the cucumber-core jar under new package names. ([#179](https://github.com/cucumber/cucumber-jvm/issues/179) Aslak Hellesøy)
* [Core] Fail if no features are found ([#163](https://github.com/cucumber/cucumber-jvm/issues/163) Aslak Hellesøy)
* [Core] Fail if duplicate features are detected ([#165](https://github.com/cucumber/cucumber-jvm/issues/165) Aslak Hellesøy)

## [1.0.0.RC14](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC13...v1.0.0.RC14) (2012-02-06)

* [Core] HTML formatter produces invalid page if no features ([#191](https://github.com/cucumber/cucumber-jvm/issues/191) Paolo Ambrosio)
* [Core] i18n java snippets for undefined steps are always generated with @Given annotation ([#184](https://github.com/cucumber/cucumber-jvm/issues/184) Vladimir Klyushnikov)
* [JUnit] Enhanced JUnit Exception Reporting ([#185](https://github.com/cucumber/cucumber-jvm/pull/185) Klaus Bayrhammer)
* [Guice] Constructor dependency resolution causes errors in GuiceFactory ([#189](https://github.com/cucumber/cucumber-jvm/issues/189) Matt Nathan)

## [1.0.0.RC13](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC12...v1.0.0.RC13) (2012-01-26)

* [Clojure] Fixed hooks ([#175](https://github.com/cucumber/cucumber-jvm/pull/175) Ronaldo M. Ferraz)
* [Core] Properly flush and close formatters ([#173](https://github.com/cucumber/cucumber-jvm/pull/173) Aslak Hellesøy, David Kowis)
* [Core] Use Gherkin's internal Gson (Aslak Hellesøy)
* [JUnit] Better reporting of Before and After blocks (Aslak Hellesøy)
* [Core] Bugfix: Scenario Outlines failing ([#170](https://github.com/cucumber/cucumber-jvm/issues/170) David Kowis, Aslak Hellesøy)
* [OpenEJB] It's back (was excluded from previous releases because it depended on unreleased libs). (Aslak Hellesøy)

## [1.0.0.RC12](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC11...v1.0.0.RC12) (2012-01-23)

* [JUnit] Tagged hooks are executed properly (Aslak Hellesøy)
* [JRuby] Better support for World blocks ([#166](https://github.com/cucumber/cucumber-jvm/pull/166) David Kowis)
* [Java] GluePath can be a package name ([#164](https://github.com/cucumber/cucumber-jvm/issues/164) Aslak Hellesøy)
* [Build] Fixed subtle path issues on Windows
* [Build] Fixed Build Failure: Cucumber-JVM: Scala (FAILURE) ([#167](https://github.com/cucumber/cucumber-jvm/issues/167) Aslak Hellesøy)

## [1.0.0.RC11](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC6...v1.0.0.RC11) (2012-01-21)

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
