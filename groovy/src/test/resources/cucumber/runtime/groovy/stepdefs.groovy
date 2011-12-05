package cucumber.runtime.groovy

import static groovy.util.GroovyTestCase.assertEquals

this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

World {
    new CustomWorld();
}

Given(~'^I have (\\d+) cukes in my belly') { int cukes ->
    haveCukes(cukes)
    lastAte('cukes')
}

Given(~'^I have (\\d+) apples in my belly') { int apples ->
    lastAte('apples')
}

Given(~'^a big basket with cukes') { ->
}

Then(~'^there are (\\d+) cukes in my belly') { int cukes ->
    checkCukes(cukes)
}

Then(~'^the (.*) contains (.*)') { String container, String ingredient ->
    assertEquals("glass", container)
}

Then(~'^I add (.*)') { String liquid ->
    assertEquals("milk", liquid)
}

Then(~'^I should be (.*)') { String mood ->
    assertEquals(mood, getMood())
}
