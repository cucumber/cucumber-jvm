package cucumber.runtime.groovy

import static groovy.util.GroovyTestCase.assertEquals

this.metaClass.mixin(cucumber.api.groovy.EN)
this.metaClass.mixin(cucumber.api.groovy.Hooks)

World {
    new CustomWorld();
}

Given(~'^I have (\\d+) apples in my belly') { int apples ->
    lastAte('apples')
}

Given(~'^a big basket with cukes') { ->
}

Given(~'^the following table:$') { table ->
    things = table.asList(Thing)
    assertEquals("Cucumber-JVM", things[1].name)
}

Given(~'^this should be converted to a list:(.+)$') { List list ->
    assertEquals(3, list.size())
    assertEquals("Cucumber-JVM", list.get(0))
    assertEquals("Cucumber", list.get(1))
}

class Thing {
    Integer year
    String name
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
