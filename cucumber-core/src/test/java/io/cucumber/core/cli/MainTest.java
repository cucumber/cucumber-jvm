package io.cucumber.core.cli;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.StaticHookDefinition;
import io.cucumber.core.runner.TestBackendSupplier;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubFeatureSupplier;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainTest {

    @Test
    void execute_returns_success_when_runtime_completes() {
        Runtime runtime = Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier())
                .withBackendSupplier(new TestBackendSupplier() {
                })
                .build();

        assertThat(Main.execute(runtime), is(equalTo((byte) 0)));
    }

    @Test
    void execute_returns_error_when_after_all_hook_throws() {
        RuntimeException afterAllFailure = new RuntimeException("AfterAll failed");
        BackendSupplier backendSupplier = new TestBackendSupplier() {
            @Override
            public void loadGlue(Glue glue, GlueDiscoveryRequest request) {
                glue.addAfterAllHook(staticHook(() -> {
                    throw afterAllFailure;
                }));
            }
        };
        Runtime runtime = Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier())
                .withBackendSupplier(backendSupplier)
                .build();

        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            byte exitStatus = assertDoesNotThrow(() -> Main.execute(runtime));
            assertThat(exitStatus, is(equalTo((byte) 1)));
        } finally {
            System.setErr(originalErr);
        }
        assertThat(captured.toString(StandardCharsets.UTF_8), containsString("AfterAll failed"));
    }

    /**
     * Regression for #3104: an exception from {@code @AfterAll} used to skip
     * {@code System.exit}, leaving non-daemon threads (e.g. {@link Timer})
     * keeping the JVM alive. {@link Main#execute} must still return an exit
     * status so {@link Main#main} can exit.
     */
    @Test
    void execute_returns_error_when_after_all_throws_with_non_daemon_timer() {
        Timer nonDaemonTimer = new Timer(false);
        try {
            BackendSupplier backendSupplier = new TestBackendSupplier() {
                @Override
                public void loadGlue(Glue glue, GlueDiscoveryRequest request) {
                    glue.addBeforeAllHook(staticHook(() -> nonDaemonTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            // keeps non-daemon thread alive until cancel
                        }
                    }, 10, 1000)));
                    glue.addAfterAllHook(staticHook(() -> {
                        throw new AssertionError("coverage below threshold");
                    }));
                }
            };
            Runtime runtime = Runtime.builder()
                    .withFeatureSupplier(new StubFeatureSupplier())
                    .withBackendSupplier(backendSupplier)
                    .build();

            PrintStream originalErr = System.err;
            System.setErr(new PrintStream(new ByteArrayOutputStream()));
            try {
                byte exitStatus = assertDoesNotThrow(() -> Main.execute(runtime));
                assertThat(exitStatus, is(equalTo((byte) 1)));
            } finally {
                System.setErr(originalErr);
            }
        } finally {
            nonDaemonTimer.cancel();
        }
    }

    private static StaticHookDefinition staticHook(Runnable action) {
        return new StaticHookDefinition() {
            @Override
            public void execute() {
                action.run();
            }

            @Override
            public int getOrder() {
                return 0;
            }

            @Override
            public boolean isDefinedAt(StackTraceElement stackTraceElement) {
                return false;
            }

            @Override
            public String getLocation() {
                return "MainTest static hook";
            }
        };
    }

}
