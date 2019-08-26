package cucumber.runtime.groovy
this.metaClass.mixin(cucumber.api.groovy.EN)

// Step definitions without parameters must explicitly define an empty list of parameters.
Given(~"Carbon Coder is running correctly\$") { ->
}