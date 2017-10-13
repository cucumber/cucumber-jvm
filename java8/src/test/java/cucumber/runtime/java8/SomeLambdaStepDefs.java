package cucumber.runtime.java8;

import cucumber.api.java8.En;

final class SomeLambdaStepDefs implements En {

    SomeLambdaStepDefs() {
        Given("I have a some step definition", () -> {
            throw new Exception();
        });
    }

}
