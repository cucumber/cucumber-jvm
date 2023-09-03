package io.cucumber.opentelemetry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseEvent;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class OpenTelemetryTracingEventListener implements ConcurrentEventListener {
    // FUTURE: Make this configurable
    public static final boolean START_NEW_TRACE_FOR_EACH_TEST_CASE = true;

    public static final boolean DEBUG_SPAN_SCOPE = false;

    // Match code locations of the form: <package>.<class>.<method>(<args>)
    // $1 = package, $2 = class, $3 = method, $4 = args (if any)
    public static final Pattern CODE_LOCATION_CLASS_METHOD_PATTERN = Pattern
            .compile("^([^(]*)\\.([^.(]*)\\.([^.(]*)\\((.*)\\)$");

    // OpenTelemetry attribute keys that are not in the standard trace semantic
    // conventions.
    public static final AttributeKey<String> ATTRIBUTE_KEY_CUCUMBER_EVENT = AttributeKey.stringKey("cucumber.event");
    public static final AttributeKey<String> ATTRIBUTE_KEY_CUCUMBER_STATUS = AttributeKey.stringKey("cucumber.status");
    public static final AttributeKey<List<String>> ATTRIBUTE_KEY_CODE_FUNCTION_ARGS = AttributeKey
            .stringArrayKey("code.functionargs");
    public static final AttributeKey<String> ATTRIBUTE_KEY_CODE_LOCATION = AttributeKey.stringKey("code.location");
    public static final AttributeKey<List<String>> ATTRIBUTE_KEY_SOURCE_TAGS = AttributeKey
            .stringArrayKey("source.tags");
    public static final AttributeKey<String> ATTRIBUTE_KEY_CASE_KEYWORD = AttributeKey.stringKey("testcase.keyword");
    public static final AttributeKey<String> ATTRIBUTE_KEY_CASE_NAME = AttributeKey.stringKey("testcase.name");
    public static final AttributeKey<String> ATTRIBUTE_KEY_CASE_ID = AttributeKey.stringKey("testcase.id");
    public static final AttributeKey<String> ATTRIBUTE_KEY_STEP_TYPE = AttributeKey.stringKey("step.type");
    public static final AttributeKey<String> ATTRIBUTE_KEY_STEP_PATTERN = AttributeKey.stringKey("step.pattern");
    public static final AttributeKey<String> ATTRIBUTE_KEY_STEP_KEYWORD = AttributeKey.stringKey("step.keyword");
    public static final AttributeKey<String> ATTRIBUTE_KEY_STEP_TEXT = AttributeKey.stringKey("step.text");
    public static final AttributeKey<String> ATTRIBUTE_KEY_STEP_ARGUMENT = AttributeKey.stringKey("step.argument");
    public static final AttributeKey<String> ATTRIBUTE_KEY_HOOK_TYPE = AttributeKey.stringKey("hook.type");

    private Tracer tracer = null; // do not use directly; call getTracer

    // Multiple steps might be executing in parallel, so we might have multiple
    // Spans/Scopes open at once. This is where we track each individually.
    // XXX we might be able to simplify this to Map<Span, Scope> and get the
    // current span with Span.current()
    private final Map<Pair<UUID, String>, Pair<Span, Scope>> testCaseToSpanScope = new HashMap<>();

    public OpenTelemetryTracingEventListener() {
        if (DEBUG_SPAN_SCOPE) {
            Thread hook = new Thread(() -> {
                if (!testCaseToSpanScope.isEmpty()) {
                    System.err.println(getClass().getSimpleName() + ": "
                            + "Test case to Span/Scope Map is not empty. Contents: "
                            + testCaseToSpanScope);
                    for (Pair<Span, Scope> spanScope : testCaseToSpanScope.values()) {
                        spanScope.getKey().addEvent("JVM shutting down while span was not completed");
                        spanScope.getValue().close();
                        spanScope.getKey().end();
                    }
                }
            });
            Runtime.getRuntime().addShutdownHook(hook);
        }
    }

    /*
     * We defer calling GlobalOpenTelemetry.getTracer as late as we can to allow
     * for the possibility of a @BeforeAll hook to initialize the SDK. Plugins
     * seem to get initialized before @BeforeAll hooks run, so we can't call
     * GlobalOpenTelemetry.getTracer at class initialization time.
     */
    private Tracer getTracer() {
        if (tracer == null) {
            tracer = GlobalOpenTelemetry.getTracer(getClass().getPackageName());
        }
        return tracer;
    }

    @Override
    public void setEventPublisher(final EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseStarted.class, this::onTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onTestCaseFinished);
        publisher.registerHandlerFor(TestStepStarted.class, this::onTestStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::onTestStepFinished);
    }

    private void onTestCaseStarted(TestCaseStarted event) {
        final SpanBuilder spanBuilder = getTracer()
                .spanBuilder(event.getTestCase().getKeyword() + ": " + event.getTestCase().getName())
                .setAttribute(ATTRIBUTE_KEY_CASE_KEYWORD, event.getTestCase().getKeyword())
                .setAttribute(ATTRIBUTE_KEY_CASE_NAME, event.getTestCase().getName())
                .setAttribute(ATTRIBUTE_KEY_CASE_ID, event.getTestCase().getId().toString())
                .setAttribute(SemanticAttributes.CODE_FILEPATH, event.getTestCase().getUri().toString())
                .setAttribute(SemanticAttributes.CODE_LINENO, (long) event.getTestCase().getLocation().getLine())
                .setAttribute(SemanticAttributes.CODE_COLUMN, (long) event.getTestCase().getLocation().getColumn())
                .setAttribute(ATTRIBUTE_KEY_SOURCE_TAGS, event.getTestCase().getTags());

        if (START_NEW_TRACE_FOR_EACH_TEST_CASE) {
            spanBuilder.setNoParent();

            if (Span.current().getSpanContext().isValid()) {
                spanBuilder.addLink(Span.current().getSpanContext());
            }
        }

        startSpan(event, fileColonLine(event), spanBuilder);
    }

    private void onTestCaseFinished(TestCaseFinished event) {
        endSpan(event, fileColonLine(event), event.getResult());
    }

    private void onTestStepStarted(TestStepStarted event) {
        final SpanBuilder spanBuilder;

        final TestStep step = event.getTestStep();
        if (step instanceof PickleStepTestStep) {
            final PickleStepTestStep pickle = (PickleStepTestStep) step;
            spanBuilder = getTracer().spanBuilder(pickle.getStep().getKeyword() + " " + pickle.getStep().getText())
                    .setAttribute(ATTRIBUTE_KEY_STEP_TYPE, "pickle")
                    .setAttribute(ATTRIBUTE_KEY_STEP_KEYWORD, pickle.getStep().getKeyword())
                    .setAttribute(ATTRIBUTE_KEY_STEP_TEXT, pickle.getStep().getText())
                    .setAttribute(ATTRIBUTE_KEY_STEP_PATTERN, pickle.getPattern())
                    .setAttribute(SemanticAttributes.CODE_FILEPATH, pickle.getUri().toString())
                    .setAttribute(SemanticAttributes.CODE_LINENO, (long) pickle.getStep().getLocation().getLine())
                    .setAttribute(SemanticAttributes.CODE_COLUMN, (long) pickle.getStep().getLocation().getColumn());

            if (pickle.getStep().getArgument() != null) {
                spanBuilder.setAttribute(ATTRIBUTE_KEY_STEP_ARGUMENT, pickle.getStep().getArgument().toString());
            }
        } else if (event.getTestStep() instanceof HookTestStep) {
            final HookTestStep hook = (HookTestStep) event.getTestStep();
            spanBuilder = getTracer()
                    .spanBuilder(hook.getHookType().name() + " hook " + getHookDescription(event))
                    .setAttribute(ATTRIBUTE_KEY_STEP_TYPE, "hook")
                    .setAttribute(ATTRIBUTE_KEY_HOOK_TYPE, hook.getHookType().name());
        } else {
            spanBuilder = getTracer()
                    .spanBuilder(
                        event.getClass().getSimpleName() + " TestStep event " + event.getTestStep().getCodeLocation())
                    .setAttribute(ATTRIBUTE_KEY_STEP_TYPE, step.getClass().getSimpleName());
        }
        startSpan(event, event.getTestStep().getCodeLocation(), spanBuilder);
    }

    private void onTestStepFinished(final TestStepFinished event) {
        endSpan(event, event.getTestStep().getCodeLocation(), event.getResult());
    }

    private static String fileColonLine(TestCaseEvent event) {
        return event.getTestCase().getUri().getSchemeSpecificPart() + ":" + event.getTestCase().getLocation().getLine();
    }

    private static String getHookDescription(TestStepStarted event) {
        final Matcher m = CODE_LOCATION_CLASS_METHOD_PATTERN.matcher(event.getTestStep().getCodeLocation());
        if (m.matches()) {
            // Just the class name (without package) and method
            return m.group(2) + "." + m.group(3);
        } else {
            return event.getTestStep().getCodeLocation();
        }
    }

    private void startSpan(TestCaseEvent event, String codeLocation, SpanBuilder spanBuilder) {
        spanBuilder.setAttribute(ATTRIBUTE_KEY_CUCUMBER_EVENT, event.getClass().getSimpleName());

        final Matcher m = CODE_LOCATION_CLASS_METHOD_PATTERN.matcher(codeLocation);
        if (m.matches()) {
            spanBuilder.setAttribute(SemanticAttributes.CODE_NAMESPACE, m.group(1) + "." + m.group(2));
            spanBuilder.setAttribute(SemanticAttributes.CODE_FUNCTION, m.group(3));
            if (!m.group(4).isEmpty()) {
                spanBuilder.setAttribute(ATTRIBUTE_KEY_CODE_FUNCTION_ARGS,
                    Arrays.asList(m.group(4).split(",\\s*")));
            }
        } else {
            spanBuilder.setAttribute(ATTRIBUTE_KEY_CODE_LOCATION, codeLocation);
        }

        final Span span = spanBuilder.startSpan();
        final Scope scope = span.makeCurrent();

        testCaseToSpanScope.put(Pair.of(event.getTestCase().getId(), codeLocation), Pair.of(span, scope));
    }

    private void endSpan(TestCaseEvent event, String codeLocation, Result result) {
        final Span span = Span.current();

        span.setAttribute(ATTRIBUTE_KEY_CUCUMBER_STATUS, result.getStatus().toString());

        if (result.getStatus() == Status.FAILED) {
            final Throwable error = result.getError();
            span.recordException(error);
            span.setStatus(StatusCode.ERROR, error.getMessage());
        } else if (result.getStatus() == Status.PASSED) {
            span.setStatus(StatusCode.OK);
        }

        final Pair<Span, Scope> spanScope = testCaseToSpanScope
                .remove(Pair.of(event.getTestCase().getId(), codeLocation));
        if (spanScope != null) {
            if (DEBUG_SPAN_SCOPE) {
                if (!span.equals(spanScope.getKey())) {
                    System.err.println(getClass().getSimpleName() + ": " +
                            "endSpan spans don't match:\n" + span.hashCode() + " (" + span.getSpanContext().toString()
                            + ")\n" + spanScope.getKey().hashCode() + " ("
                            + spanScope.getKey().getSpanContext().toString() + ")");
                }
            }
            spanScope.getValue().close();
            spanScope.getKey().end();
        } else {
            if (DEBUG_SPAN_SCOPE) {
                System.err.println(getClass().getSimpleName() + ": "
                        + "Could not find stored Span and Scope for " + event.getTestCase().getId() + "/"
                        + codeLocation + " (current span is " + span + ")");
            }
        }
    }

    private static class Pair<K, V> {
        private final K k;
        private final V v;

        private Pair(K k, V v) {
            this.k = k;
            this.v = v;
        }

        public static <K, V> Pair<K, V> of(K k, V v) {
            return new Pair<>(k, v);
        }

        public K getKey() {
            return k;
        }

        public V getValue() {
            return v;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(getKey(), pair.getKey()) && Objects.equals(getValue(), pair.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getKey(), getValue());
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "key=" + getKey() +
                    ", value=" + getValue() +
                    '}';
        }
    }
}
