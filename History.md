## [Git master branch](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC16...master)

* [JUnit] Improved JUnit runner. ([#107](https://github.com/cucumber/cucumber-jvm/issues/107), [#211](https://github.com/cucumber/cucumber-jvm/issues/211), [#216](https://github.com/cucumber/cucumber-jvm/pull/216) Giso Deutschmann)
* [Core] Stacktrace filtering filters away too much. ([#228](https://github.com/cucumber/cucumber-jvm/issues/228) Aslak Hellesøy)
* [Groovy] Fix native Groovy cucumber CLI ([#212](https://github.com/cucumber/cucumber-jvm/issues/212) Martin Hauner)
* [Core] Indeterministic feature ordering on Unix ([#224](https://github.com/cucumber/cucumber-jvm/issues/224) hutchy2570)

## [1.0.0.RC16](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC15...v1.0.0.RC16)

* [Core] Embed text and images in reports. ([#205](https://github.com/cucumber/cucumber-jvm/issues/205) Aslak Hellesøy)
* [Core] Detect duplicate step definitions. (Aslak Hellesøy)
* [Java] Auto-generated step definitions should escape dollar signs / other regex chars ([#204](https://github.com/cucumber/cucumber-jvm/issues/204), [#215](https://github.com/cucumber/cucumber-jvm/pull/215) Ian Dees)
* [Core] Scenario Outlines work with tagged hooks. ([#209](https://github.com/cucumber/cucumber-jvm/issues/209), [#210](https://github.com/cucumber/cucumber-jvm/issues/210) Aslak Hellesøy)
* [Spring] Allowed customization of Spring step definitions context ([#203](https://github.com/cucumber/cucumber-jvm/pull/203) vladimirkl)
* [Core] Ambiguous step definitions don't cause Cucumber to blow up, they just fail the step. (Aslak Hellesøy)
* [Java] Fixed NullPointerException in ClasspathMethodScanner ([#201](https://github.com/cucumber/cucumber-jvm/pull/201) vladimirkl)
* [Groovy] Compiled Groovy stepdef scripts are found as well as source ones (Aslak Hellesøy)
* [Jython] I18n translations for most languages. Languages that can't be transformed to ASCII are excluded. ([#176](https://github.com/cucumber/cucumber-jvm/issues/176), [#197](https://github.com/cucumber/cucumber-jvm/pull/197) Stephen Abrams)

## [1.0.0.RC15](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC14...v1.0.0.RC15)

* [Java] You must use `cucumber.runtime.xstream` instead of `com.thoughtworks.xstream` for custom converters.
* [Core] XStream and Diffutils are now packaged inside the cucumber-core jar under new package names. ([#179](https://github.com/cucumber/cucumber-jvm/issues/179) Aslak Hellesøy)
* [Core] Fail if no features are found ([#163](https://github.com/cucumber/cucumber-jvm/issues/163) Aslak Hellesøy)
* [Core] Fail if duplicate features are detected ([#165](https://github.com/cucumber/cucumber-jvm/issues/165) Aslak Hellesøy)

## [1.0.0.RC14](https://github.com/cucumber/cucumber-jvm/compare/v1.0.0.RC13...v1.0.0.RC14)

* [Core] HTML formatter produces invalid page if no features ([#191](https://github.com/cucumber/cucumber-jvm/issues/191) Paolo Ambrosio)
* [Core] i18n java snippets for undefined steps are always generated with @Given annotation ([#184](https://github.com/cucumber/cucumber-jvm/issues/184) vladimirkl)
* [JUnit] Enhanced JUnit Exception Reporting ([#185](https://github.com/cucumber/cucumber-jvm/pull/185) klausbayrhammer)
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