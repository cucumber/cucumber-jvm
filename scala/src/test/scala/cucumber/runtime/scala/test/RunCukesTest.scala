package cucumber.runtime.scala.test

import _root_.org.junit.runner.RunWith
import _root_.cucumber.api.junit.Cucumber
import gherkin.formatter.model.Feature

@RunWith(classOf[Cucumber])
@Cucumber.Options(tags = Array("@SmokeTest"), strict=true)
class RunCukesTest