package io.cucumber.compatibility.skippedexception;

import io.cucumber.java.en.And;
import org.junit.jupiter.api.Assumptions;

public final class SkippedException {

    @And("I skip a step")
    public void iSkipAStep() {
        Assumptions.abort("skipping");
    }
}
