package cucumber.runtime.groovy

import groovy.transform.Field

metaClass.mixin(cucumber.api.groovy.EN)

@Field int invocationCount = 0

def setup() {
    invocationCount++
}

When(~'^a script with setup method is under use$') { ->
    // doesn't make sense, just for better step readability
}

Then(~'^setup must be invoked once$') { ->
    assert "Invocation count is 1", 1 == invocationCount
}