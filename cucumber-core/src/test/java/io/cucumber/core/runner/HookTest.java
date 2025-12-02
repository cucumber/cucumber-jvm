package io.cucumber.core.runner;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.snippets.TestSnippet;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HookTest {

    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
    private final RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
    private final Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n");
    private final Pickle pickle = feature.getPickles().get(0);

    /**
     * Test for
     * <a href="https://github.com/cucumber/cucumber-jvm/issues/23">#23</a>.
     */
    @Test
    void after_hooks_execute_before_objects_are_disposed() {
        final List<String> eventListener = new ArrayList<>();
        final HookDefinition hook = new MockHookDefinition("", "hook-location", eventListener);
        Backend backend = new StubBackend(hook, eventListener);
        ObjectFactory objectFactory = new StubObjectFactory();
        Runner runner = new Runner(bus, Collections.singleton(backend), objectFactory, runtimeOptions);

        runner.runPickle(pickle);

        assertLinesMatch(eventListener, List.of("buildWorld", "execute", "disposeWorld"));
    }

    @Test
    void hook_throws_exception_with_name_when_tag_expression_is_invalid() {
        final List<String> eventListener = new ArrayList<>();
        final HookDefinition hook = new MockHookDefinition("(", "hook-location", eventListener);
        Backend backend = new StubBackend(hook, eventListener);
        ObjectFactory objectFactory = new StubObjectFactory();

        RuntimeException e = assertThrows(RuntimeException.class,
            () -> new Runner(bus, Collections.singleton(backend), objectFactory,
                runtimeOptions));

        assertThat(e.getMessage(),
            is("Invalid tag expression at 'hook-location'"));
    }

    private static class StubObjectFactory implements ObjectFactory {
        @Override
        public boolean addClass(Class<?> glueClass) {
            return false;
        }

        @Override
        public <T> T getInstance(Class<T> glueClass) {
            return null;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }
    }

    private final static class StubBackend implements Backend {
        private final HookDefinition beforeHook;
        private final List<String> eventListener;

        public StubBackend(HookDefinition beforeHook, List<String> eventListener) {
            this.beforeHook = beforeHook;
            this.eventListener = eventListener;
        }

        @Override
        public void loadGlue(Glue glue, List<URI> gluePaths) {
            glue.addBeforeHook(beforeHook);
        }

        @Override
        public void buildWorld() {
            eventListener.add("buildWorld");
        }

        @Override
        public void disposeWorld() {
            eventListener.add("disposeWorld");
        }

        @Override
        public Snippet getSnippet() {
            return new TestSnippet();
        }
    }

    private static final class MockHookDefinition implements HookDefinition {
        private final String tagExpression;
        private final String location;
        private final List<String> eventListener;

        public MockHookDefinition(String tagExpression, String location, List<String> eventListener) {
            this.tagExpression = tagExpression;
            this.location = location;
            this.eventListener = eventListener;
        }

        @Override
        public void execute(TestCaseState state) {
            eventListener.add("execute");
        }

        @Override
        public String getTagExpression() {
            return tagExpression;
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
            return location;
        }
    }
}
