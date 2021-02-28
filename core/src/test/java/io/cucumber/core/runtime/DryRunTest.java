package io.cucumber.core.runtime;

import io.cucumber.core.backend.StubPendingException;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStepFinished;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class DryRunTest {

    Feature skip = TestFeatureParser.parse("1/skipped.feature",
        "Feature: skip\n" +
                "  Scenario: skip\n" +
                "    * skipped step\n" +
                "    * passed step\n" +
                "    * skipped step\n" +
                "    * pending step\n" +
                "    * undefined step\n" +
                "    * ambiguous step\n" +
                "    * failed step\n");
    Feature pending = TestFeatureParser.parse("2/pending.feature",
        "Feature: pending\n" +
                "  Scenario: pending\n" +
                "    * pending step\n" +
                "    * passed step\n" +
                "    * skipped step\n" +
                "    * pending step\n" +
                "    * undefined step\n" +
                "    * ambiguous step\n" +
                "    * failed step\n");
    Feature undefined = TestFeatureParser.parse("3/undefined.feature",
        "Feature: undefined\n" +
                "  Scenario: undefined\n" +
                "    * undefined step\n" +
                "    * passed step\n" +
                "    * skipped step\n" +
                "    * pending step\n" +
                "    * undefined step\n" +
                "    * ambiguous step\n" +
                "    * failed step\n");
    Feature ambiguous = TestFeatureParser.parse("4/ambiguous.feature",
        "Feature: ambiguous\n" +
                "  Scenario: ambiguous\n" +
                "    * ambiguous step\n" +
                "    * passed step\n" +
                "    * skipped step\n" +
                "    * pending step\n" +
                "    * undefined step\n" +
                "    * ambiguous step\n" +
                "    * failed step\n");
    Feature failed = TestFeatureParser.parse("5/failed.feature",
        "Feature: failed\n" +
                "  Scenario: failed\n" +
                "    * failed step\n" +
                "    * passed step\n" +
                "    * skipped step\n" +
                "    * pending step\n" +
                "    * undefined step\n" +
                "    * ambiguous step\n" +
                "    * failed step\n");

    StubBackendSupplier backend = new StubBackendSupplier(
        new StubStepDefinition("passed step"),
        new StubStepDefinition("skipped step", new TestAbortedException()),
        new StubStepDefinition("^ambiguous step.*$"),
        new StubStepDefinition("^.*ambiguous step$"),
        new StubStepDefinition("pending step", new StubPendingException()),
        new StubStepDefinition("failed step", new RuntimeException()));

    @Test
    void run_skips_all_steps_non_passing_step() {
        StepStatusSpy out = new StepStatusSpy();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(skip, pending, undefined, ambiguous, failed))
                .withAdditionalPlugins(out)
                .withBackendSupplier(backend)
                .build()
                .run();

        assertThat(out.toString(), is("" +
                "skip\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "pending\n" +
                "    PENDING\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "undefined\n" +
                "    UNDEFINED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "ambiguous\n" +
                "    AMBIGUOUS\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "failed\n" +
                "    FAILED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n"));

    }

    @Test
    void dry_run_passes_skipped_step() {
        Feature skipped = TestFeatureParser.parse("1/skipped.feature",
            "Feature: skipped\n" +
                    "  Scenario: skipped\n" +
                    "    * skipped step\n");

        StepStatusSpy out = new StepStatusSpy();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(skipped))
                .withAdditionalPlugins(out)
                .withBackendSupplier(backend)
                .withRuntimeOptions(new RuntimeOptionsBuilder().setDryRun().build())
                .build()
                .run();

        assertThat(out.toString(), is("" +
                "skipped\n" +
                "    PASSED\n"));
    }

    @Test
    void dry_run_passes_pending_step() {
        Feature pending = TestFeatureParser.parse("2/pending.feature",
            "Feature: pending\n" +
                    "  Scenario: pending\n" +
                    "    * pending step\n");

        StepStatusSpy out = new StepStatusSpy();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(pending))
                .withAdditionalPlugins(out)
                .withBackendSupplier(backend)
                .withRuntimeOptions(new RuntimeOptionsBuilder().setDryRun().build())
                .build()
                .run();

        assertThat(out.toString(), is("" +
                "pending\n" +
                "    PASSED\n"));
    }

    @Test
    void dry_run_skips_all_steps_after_undefined_step() {
        StepStatusSpy out = new StepStatusSpy();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(undefined))
                .withAdditionalPlugins(out)
                .withBackendSupplier(backend)
                .withRuntimeOptions(new RuntimeOptionsBuilder().setDryRun().build())
                .build()
                .run();

        assertThat(out.toString(), is("" +
                "undefined\n" +
                "    UNDEFINED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n"));
    }

    @Test
    void dry_run_skips_all_steps_after_ambiguous_step() {
        StepStatusSpy out = new StepStatusSpy();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(ambiguous))
                .withAdditionalPlugins(out)
                .withBackendSupplier(backend)
                .withRuntimeOptions(new RuntimeOptionsBuilder().setDryRun().build())
                .build()
                .run();

        assertThat(out.toString(), is("" +
                "ambiguous\n" +
                "    AMBIGUOUS\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n" +
                "    SKIPPED\n"));
    }

    @Test
    void dry_run_passes_failed_step() {
        Feature failed = TestFeatureParser.parse("5/failed.feature",
            "Feature: failed\n" +
                    "  Scenario: failed\n" +
                    "    * failed step\n");

        StepStatusSpy out = new StepStatusSpy();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(failed))
                .withAdditionalPlugins(out)
                .withBackendSupplier(backend)
                .withRuntimeOptions(new RuntimeOptionsBuilder().setDryRun().build())
                .build()
                .run();

        assertThat(out.toString(), is("" +
                "failed\n" +
                "    PASSED\n"));
    }

    private static class StepStatusSpy implements EventListener {

        private final StringBuilder calls = new StringBuilder();

        @Override
        public void setEventPublisher(EventPublisher publisher) {
            publisher.registerHandlerFor(TestCaseStarted.class,
                event -> calls.append(event.getTestCase().getName()).append("\n"));
            publisher.registerHandlerFor(TestStepFinished.class,
                event -> calls.append("    ").append(event.getResult().getStatus()).append("\n"));
        }

        @Override
        public String toString() {
            return calls.toString();
        }

    }

}
