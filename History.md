## In Git

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