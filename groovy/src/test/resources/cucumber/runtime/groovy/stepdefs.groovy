package cucumber.runtime.groovy

import static groovy.util.GroovyTestCase.assertEquals

this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

class CustomWorld {
    private def cukes

    def haveCukes(n) {
        cukes = n
    }

    def checkCukes(n) {
        assertEquals(cukes, n)
    }
}

World {
    new CustomWorld();
}

Given(~"^I have (\\d+) cukes in my belly") { String cukes ->
    haveCukes(cukes)
}

Then(~"^there are (\\d+) cukes in my belly") { String cukes ->
    checkCukes(cukes)
}

Then(~"^the (.*) contains (.*)") { String container, String ingredient ->
    assertEquals("glass", container)
}

Then(~"^I add (.*)") { String liquid ->
    assertEquals("milk", liquid)
}
