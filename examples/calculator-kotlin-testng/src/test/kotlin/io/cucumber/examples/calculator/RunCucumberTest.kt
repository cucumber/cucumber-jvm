package io.cucumber.examples.calculator

import io.cucumber.testng.AbstractTestNGCucumberTests
import io.cucumber.testng.CucumberOptions
import org.testng.annotations.DataProvider


@CucumberOptions(
    plugin = ["html:target/results.html"],
//    tags = "@shopping"
)
class RunCucumberTest : AbstractTestNGCucumberTests() {

    @DataProvider
    override fun scenarios(): Array<Array<Any>> {
        return super.scenarios()
    }

}
