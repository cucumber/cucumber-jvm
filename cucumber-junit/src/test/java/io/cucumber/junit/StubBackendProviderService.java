package io.cucumber.junit;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.backend.StaticHookDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.TestCaseState;

import java.lang.reflect.Type;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StubBackendProviderService implements BackendProviderService {

    static final List<Consumer<String>> callbacks = new ArrayList<>();

    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> classLoader) {
        return new StubBackend();
    }

    /**
     * We need an implementation of Backend to prevent Runtime from blowing up.
     */
    public static class StubBackend implements Backend {

        StubBackend() {

        }

        @Override
        public void loadGlue(Glue glue, List<URI> gluePaths) {
            glue.addStepDefinition(createStepDefinition("first step"));
            glue.addStepDefinition(createStepDefinition("second step"));
            glue.addStepDefinition(createStepDefinition("third step"));
            glue.addStepDefinition(createStepDefinition("background step"));
            glue.addStepDefinition(createStepDefinition("scenario name"));
            glue.addStepDefinition(createStepDefinition("scenario A"));
            glue.addStepDefinition(createStepDefinition("scenario B"));
            glue.addStepDefinition(createStepDefinition("scenario C"));
            glue.addStepDefinition(createStepDefinition("scenario D"));
            glue.addStepDefinition(createStepDefinition("scenario E"));

            glue.addStepDefinition(createStepDefinition("a single scenario"));
            glue.addStepDefinition(createStepDefinition("it is executed"));
            glue.addStepDefinition(createStepDefinition("nothing else happens"));
            glue.addStepDefinition(createStepDefinition("a scenario"));
            glue.addStepDefinition(createStepDefinition("is only runs once"));
            glue.addStepDefinition(createStepDefinition("a scenario outline"));
            glue.addStepDefinition(createStepDefinition("A is used"));
            glue.addStepDefinition(createStepDefinition("B is used"));
            glue.addStepDefinition(createStepDefinition("C is used"));
            glue.addStepDefinition(createStepDefinition("D is used"));

            glue.addBeforeAllHook(createStaticHook("BeforeAll"));
            glue.addAfterAllHook(createStaticHook("AfterAll"));
            glue.addBeforeHook(createHook("Before"));
            glue.addAfterHook(createHook("After"));

        }

        private HookDefinition createHook(String event) {
            return new HookDefinition() {
                @Override
                public void execute(TestCaseState state) {
                    callbacks.forEach(consumer -> consumer.accept(event));
                }

                @Override
                public String getTagExpression() {
                    return "";
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
                    return "stubbed location";
                }
            };
        }

        private StaticHookDefinition createStaticHook(String event) {
            return new StaticHookDefinition() {
                @Override
                public void execute() {
                    callbacks.forEach(consumer -> consumer.accept(event));
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
                    return "stubbed location";
                }
            };
        }

        private StepDefinition createStepDefinition(final String pattern) {
            return new StepDefinition() {

                @Override
                public void execute(Object[] args) {
                    callbacks.forEach(consumer -> consumer.accept("Step"));
                }

                @Override
                public List<ParameterInfo> parameterInfos() {
                    return Collections.emptyList();
                }

                @Override
                public String getPattern() {
                    return pattern;
                }

                @Override
                public boolean isDefinedAt(StackTraceElement stackTraceElement) {
                    return false;
                }

                @Override
                public String getLocation() {
                    return "stubbed location";
                }
            };
        }

        @Override
        public void buildWorld() {
        }

        @Override
        public void disposeWorld() {
        }

        @Override
        public Snippet getSnippet() {
            return new Snippet() {

                private int i = 1;

                @Override
                public MessageFormat template() {
                    return new MessageFormat("stub snippet " + i++);
                }

                @Override
                public String tableHint() {
                    return "";
                }

                @Override
                public String arguments(Map<String, Type> arguments) {
                    return "";
                }

                @Override
                public String escapePattern(String pattern) {
                    return "";
                }
            };
        }

    }

}
