package io.cucumber.core.runner;

import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.snippets.TestSnippet;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

import java.time.Clock;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        Backend backend = mock(Backend.class);
        when(backend.getSnippet()).thenReturn(new TestSnippet());
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        final HookDefinition hook = mock(HookDefinition.class);
        when(hook.getLocation()).thenReturn("hook-location");
        TypeRegistryConfigurer typeRegistryConfigurer = mock(TypeRegistryConfigurer.class);
        when(hook.getTagExpression()).thenReturn("");

        doAnswer(invocation -> {
            Glue glue = invocation.getArgument(0);
            glue.addBeforeHook(hook);
            return null;
        }).when(backend).loadGlue(any(Glue.class), ArgumentMatchers.anyList());

        Runner runner = new Runner(bus, Collections.singleton(backend), objectFactory, typeRegistryConfigurer,
            runtimeOptions);

        runner.runPickle(pickle);

        InOrder inOrder = inOrder(hook, backend);
        inOrder.verify(backend).buildWorld();
        inOrder.verify(hook).execute(ArgumentMatchers.any());
        inOrder.verify(backend).disposeWorld();
    }

    @Test
    void hook_throws_exception_with_name_when_tag_expression_is_invalid() {
        Backend backend = mock(Backend.class);
        when(backend.getSnippet()).thenReturn(new TestSnippet());
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        final HookDefinition hook = mock(HookDefinition.class);
        when(hook.getLocation()).thenReturn("hook-location");
        TypeRegistryConfigurer typeRegistryConfigurer = mock(TypeRegistryConfigurer.class);

        when(hook.getTagExpression()).thenReturn("(");

        doAnswer(invocation -> {
            Glue glue = invocation.getArgument(0);
            glue.addBeforeHook(hook);
            return null;
        }).when(backend).loadGlue(any(Glue.class), ArgumentMatchers.anyList());

        RuntimeException e = assertThrows(RuntimeException.class,
            () -> new Runner(bus, Collections.singleton(backend), objectFactory, typeRegistryConfigurer,
                runtimeOptions));

        assertThat(e.getMessage(),
            is("Invalid tag expression at 'hook-location'"));
    }
}
