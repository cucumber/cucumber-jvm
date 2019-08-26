package cucumber.runtime.groovy

this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

Given(~'^I have (\\d+) cukes in my belly') { int cukes ->
    haveCukes(cukes)
    lastAte('cukes')
}
