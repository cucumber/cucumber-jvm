package io.cucumber.junit.platform.engine;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.BackendProviderService;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.TestCaseState;

import java.lang.reflect.Type;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class StubBackendProviderService implements BackendProviderService {

    @Override
    public Backend create(Lookup lookup, Container container, Supplier<ClassLoader> resourceLoader) {
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
            glue.addStepDefinition(new StubStepDefinition("a single scenario"));
            glue.addStepDefinition(new StubStepDefinition("it is executed"));
            glue.addStepDefinition(new StubStepDefinition("nothing else happens"));
            glue.addStepDefinition(new StubStepDefinition("a scenario"));
            glue.addStepDefinition(new StubStepDefinition("is only runs once"));
            glue.addStepDefinition(new StubStepDefinition("a scenario outline"));
            glue.addStepDefinition(new StubStepDefinition("A is used"));
            glue.addStepDefinition(new StubStepDefinition("B is used"));
            glue.addStepDefinition(new StubStepDefinition("C is used"));
            glue.addStepDefinition(new StubStepDefinition("D is used"));

            StubStepHookDefinition an_attachment = new StubStepHookDefinition("an attachment");
            glue.addBeforeHook(an_attachment);
            glue.addStepDefinition(an_attachment);

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

        private static class StubStepDefinition implements StepDefinition {

            private final String pattern;

            public StubStepDefinition(String pattern) {
                this.pattern = pattern;
            }

            @Override
            public void execute(Object[] args) {

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

        }

        private static class StubStepHookDefinition implements StepDefinition, HookDefinition {

            private final String pattern;
            private TestCaseState state;

            public StubStepHookDefinition(String pattern) {
                this.pattern = pattern;
            }

            @Override
            public void execute(Object[] args) {
                state.attach("data", "mediaType", "name");
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

            @Override
            public void execute(TestCaseState state) {
                this.state = state;
            }

            @Override
            public String getTagExpression() {
                return "";
            }

            @Override
            public int getOrder() {
                return 0;
            }

        }

    }

}
