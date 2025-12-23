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
        """
                Feature: skip
                  Scenario: skip
                    * skipped step
                    * passed step
                    * skipped step
                    * pending step
                    * undefined step
                    * ambiguous step
                    * failed step
                """);
    Feature pending = TestFeatureParser.parse("2/pending.feature",
        """
                Feature: pending
                  Scenario: pending
                    * pending step
                    * passed step
                    * skipped step
                    * pending step
                    * undefined step
                    * ambiguous step
                    * failed step
                """);
    Feature undefined = TestFeatureParser.parse("3/undefined.feature",
        """
                Feature: undefined
                  Scenario: undefined
                    * undefined step
                    * passed step
                    * skipped step
                    * pending step
                    * undefined step
                    * ambiguous step
                    * failed step
                """);
    Feature ambiguous = TestFeatureParser.parse("4/ambiguous.feature",
        """
                Feature: ambiguous
                  Scenario: ambiguous
                    * ambiguous step
                    * passed step
                    * skipped step
                    * pending step
                    * undefined step
                    * ambiguous step
                    * failed step
                """);
    Feature failed = TestFeatureParser.parse("5/failed.feature",
        """
                Feature: failed
                  Scenario: failed
                    * failed step
                    * passed step
                    * skipped step
                    * pending step
                    * undefined step
                    * ambiguous step
                    * failed step
                """);

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

        assertThat(out.toString(), is("""
                skip
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                pending
                    PENDING
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                undefined
                    UNDEFINED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                ambiguous
                    AMBIGUOUS
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                failed
                    FAILED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                """));

    }

    @Test
    void dry_run_passes_skipped_step() {
        Feature skipped = TestFeatureParser.parse("1/skipped.feature",
            """
                    Feature: skipped
                      Scenario: skipped
                        * skipped step
                    """);

        StepStatusSpy out = new StepStatusSpy();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(skipped))
                .withAdditionalPlugins(out)
                .withBackendSupplier(backend)
                .withRuntimeOptions(new RuntimeOptionsBuilder().setDryRun().build())
                .build()
                .run();

        assertThat(out.toString(), is("""
                skipped
                    PASSED
                """));
    }

    @Test
    void dry_run_passes_pending_step() {
        Feature pending = TestFeatureParser.parse("2/pending.feature",
            """
                    Feature: pending
                      Scenario: pending
                        * pending step
                    """);

        StepStatusSpy out = new StepStatusSpy();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(pending))
                .withAdditionalPlugins(out)
                .withBackendSupplier(backend)
                .withRuntimeOptions(new RuntimeOptionsBuilder().setDryRun().build())
                .build()
                .run();

        assertThat(out.toString(), is("""
                pending
                    PASSED
                """));
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

        assertThat(out.toString(), is("""
                undefined
                    UNDEFINED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                """));
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

        assertThat(out.toString(), is("""
                ambiguous
                    AMBIGUOUS
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                    SKIPPED
                """));
    }

    @Test
    void dry_run_passes_failed_step() {
        Feature failed = TestFeatureParser.parse("5/failed.feature",
            """
                    Feature: failed
                      Scenario: failed
                        * failed step
                    """);

        StepStatusSpy out = new StepStatusSpy();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(failed))
                .withAdditionalPlugins(out)
                .withBackendSupplier(backend)
                .withRuntimeOptions(new RuntimeOptionsBuilder().setDryRun().build())
                .build()
                .run();

        assertThat(out.toString(), is("""
                failed
                    PASSED
                """));
    }

    private static final class StepStatusSpy implements EventListener {

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
