package io.cucumber.core.plugin;

import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.TestRunFinished;
import io.cucumber.messages.types.TestStepFinished;
import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

import static io.cucumber.core.plugin.ProgressFormatter.Ansi.Attributes.FOREGROUND_CYAN;
import static io.cucumber.core.plugin.ProgressFormatter.Ansi.Attributes.FOREGROUND_DEFAULT;
import static io.cucumber.core.plugin.ProgressFormatter.Ansi.Attributes.FOREGROUND_GREEN;
import static io.cucumber.core.plugin.ProgressFormatter.Ansi.Attributes.FOREGROUND_RED;
import static io.cucumber.core.plugin.ProgressFormatter.Ansi.Attributes.FOREGROUND_YELLOW;
import static io.cucumber.messages.types.TestStepResultStatus.AMBIGUOUS;
import static io.cucumber.messages.types.TestStepResultStatus.FAILED;
import static io.cucumber.messages.types.TestStepResultStatus.PASSED;
import static io.cucumber.messages.types.TestStepResultStatus.PENDING;
import static io.cucumber.messages.types.TestStepResultStatus.SKIPPED;
import static io.cucumber.messages.types.TestStepResultStatus.UNDEFINED;
import static java.util.Objects.requireNonNull;

/**
 * Renders a rudimentary progress bar.
 * <p>
 * Each character in the bar represents either a step or hook. The status of that
 * step or hook is indicated by the character and its color.
 */
public final class ProgressFormatter implements ConcurrentEventListener, ColorAware {

    private final Map<TestStepResultStatus, String> chars = new EnumMap<>(TestStepResultStatus.class);
    private final Map<TestStepResultStatus, Ansi> escapes = new EnumMap<>(TestStepResultStatus.class);
    private final Ansi reset = Ansi.with(FOREGROUND_DEFAULT);
    private final PrintWriter writer;
    private boolean monochrome = false;

    public ProgressFormatter(OutputStream out) {
        this.writer = createPrintWriter(out);

        chars.put(PASSED, ".");
        chars.put(UNDEFINED, "U");
        chars.put(PENDING, "P");
        chars.put(SKIPPED, "-");
        chars.put(FAILED, "F");
        chars.put(AMBIGUOUS, "A");

        escapes.put(PASSED, Ansi.with(FOREGROUND_GREEN));
        escapes.put(UNDEFINED, Ansi.with(FOREGROUND_YELLOW));
        escapes.put(PENDING, Ansi.with(FOREGROUND_YELLOW));
        escapes.put(SKIPPED, Ansi.with(FOREGROUND_CYAN));
        escapes.put(FAILED, Ansi.with(FOREGROUND_RED));
        escapes.put(AMBIGUOUS, Ansi.with(FOREGROUND_RED));
    }

    private static PrintWriter createPrintWriter(OutputStream out) {
        return new PrintWriter(
                new OutputStreamWriter(
                        requireNonNull(out),
                        StandardCharsets.UTF_8
                )
        );
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(Envelope.class, event -> {
            event.getTestStepFinished().ifPresent(this::handleTestStepFinished);
            event.getTestRunFinished().ifPresent(this::handleTestRunFinished);
        });
    }

    private void handleTestStepFinished(TestStepFinished event) {
        TestStepResultStatus status = event.getTestStepResult().getStatus();
        // Prevent tearing in output when multiple threads write to System.out
        StringBuilder buffer = new StringBuilder();
        if (!monochrome) {
            buffer.append(escapes.get(status));
        }
        buffer.append(chars.get(status));
        if (!monochrome) {
            buffer.append(reset);
        }
        writer.append(buffer);
        writer.flush();
    }

    private void handleTestRunFinished(TestRunFinished testRunFinished) {
        writer.println();
        writer.close();
    }

    /**
     * Represents an <a href="https://en.wikipedia.org/wiki/ANSI_escape_code">ANSI escape code</a> in the format {@code CSI n m}.
     */
    static final class Ansi {

        private static final char FIRST_ESCAPE = 27;
        private static final char SECOND_ESCAPE = '[';
        private static final String END_SEQUENCE = "m";
        private final String controlSequence;

        /**
         * Constructs an ANSI escape code with the given attributes.
         *
         * @param attributes to include.
         * @return an ANSI escape code with the given attributes
         */
        public static Ansi with(Ansi.Attributes... attributes) {
            return new Ansi(requireNonNull(attributes));
        }

        private Ansi(Ansi.Attributes... attributes) {
            this.controlSequence = createControlSequence(attributes);
        }

        private String createControlSequence(Ansi.Attributes... attributes) {
            StringBuilder a = new StringBuilder(attributes.length * 5);

            for (Ansi.Attributes attribute : attributes) {
                a.append(FIRST_ESCAPE).append(SECOND_ESCAPE);
                a.append(attribute.value);
                a.append(END_SEQUENCE);
            }

            return a.toString();
        }

        @Override
        public String toString() {
            return controlSequence;
        }

        /**
         * A select number of attributes from all the available 
         * <a href=https://en.wikipedia.org/wiki/ANSI_escape_code#Select_Graphic_Rendition_parameters>Select Graphic Rendition attributes</a>.
         */
        enum Attributes {

            // https://en.wikipedia.org/wiki/ANSI_escape_code#Colors
            FOREGROUND_RED(31),
            FOREGROUND_GREEN(32),
            FOREGROUND_YELLOW(33),
            FOREGROUND_CYAN(36),
            FOREGROUND_DEFAULT(39);

            private final int value;

            Attributes(int index) {
                this.value = index;
            }
        }
    }

}
