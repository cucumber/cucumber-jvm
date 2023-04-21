package io.cucumber.guice;

import io.cucumber.core.backend.*;
import io.cucumber.guice.integration.YourInjectorSource;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;

class GuiceBackendTest {

    public final Supplier<ClassLoader> classLoader = currentThread()::getContextClassLoader;

    private final Glue glue = new MockGlue();

    @Test
    void finds_injector_source_impls_by_classpath_url() {
        MockObjectFactory factory = new MockObjectFactory();
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/guice/integration")));
        assertTrue(factory.classes.contains(YourInjectorSource.class));
    }

    @Test
    void finds_injector_source_impls_once_by_classpath_url() {
        MockObjectFactory factory = new MockObjectFactory();
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        backend.loadGlue(glue, asList(URI.create("classpath:io/cucumber/guice/integration"),
            URI.create("classpath:io/cucumber/guice/integration")));
        assertTrue(factory.classes.contains(YourInjectorSource.class));
        assertEquals(1, factory.classes.size());
    }

    @Test
    void world_and_snippet_methods_do_nothing() {
        MockObjectFactory factory = new MockObjectFactory();
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/guice/integration")));
        backend.buildWorld();
        backend.disposeWorld();
        assertThat(backend.getSnippet(), is(nullValue()));
    }

    @Test
    void doesnt_save_anything_in_glue() {
        MockObjectFactory factory = new MockObjectFactory();
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        backend.loadGlue(null, singletonList(URI.create("classpath:io/cucumber/guice/integration")));
        assertTrue(factory.classes.contains(YourInjectorSource.class));
    }

    @Test()
    void list_of_uris_cant_be_null() {
        MockObjectFactory factory = new MockObjectFactory();
        GuiceBackend backend = new GuiceBackend(factory, classLoader);
        assertThrows(NullPointerException.class, () -> backend.loadGlue(glue, null));
    }

    @Test
    void backend_service_creates_backend() {
        MockObjectFactory factory = new MockObjectFactory();
        BackendProviderService backendProviderService = new GuiceBackendProviderService();
        assertThat(backendProviderService.create(factory, factory, classLoader), is(notNullValue()));
    }

    private static class MockObjectFactory implements ObjectFactory {
        List<Class<?>> classes = new ArrayList<>();
        boolean started = false;
        boolean stopped = false;

        @Override
        public boolean addClass(Class<?> glueClass) {
            return classes.add(glueClass);
        }

        @Override
        public <T> T getInstance(Class<T> glueClass) {
            return null;
        }

        @Override
        public void start() {
            started = true;
        }

        @Override
        public void stop() {
            stopped = true;
        }
    }

    private static class MockGlue implements Glue {

        @Override
        public void addBeforeAllHook(StaticHookDefinition beforeAllHook) {

        }

        @Override
        public void addAfterAllHook(StaticHookDefinition afterAllHook) {

        }

        @Override
        public void addStepDefinition(StepDefinition stepDefinition) {

        }

        @Override
        public void addBeforeHook(HookDefinition beforeHook) {

        }

        @Override
        public void addAfterHook(HookDefinition afterHook) {

        }

        @Override
        public void addBeforeStepHook(HookDefinition beforeStepHook) {

        }

        @Override
        public void addAfterStepHook(HookDefinition afterStepHook) {

        }

        @Override
        public void addParameterType(ParameterTypeDefinition parameterType) {

        }

        @Override
        public void addDataTableType(DataTableTypeDefinition dataTableType) {

        }

        @Override
        public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {

        }

        @Override
        public void addDefaultDataTableEntryTransformer(
                DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer
        ) {

        }

        @Override
        public void addDefaultDataTableCellTransformer(
                DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer
        ) {

        }

        @Override
        public void addDocStringType(DocStringTypeDefinition docStringType) {

        }
    }
}
