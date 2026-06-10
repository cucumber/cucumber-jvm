package io.cucumber.compatibility.skippedfailinghook.skipped;

import io.cucumber.java.After;
import io.cucumber.java.en.And;
import org.junit.jupiter.api.Assumptions;

public final class SkippedFailingHook {

    @And("a step that skips")
    public void aStepThatSkips() {
        Assumptions.abort();
    }

    @After
    public void after() {
        throw new RuntimeException("whoops");
    }
}
