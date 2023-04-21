package io.cucumber.spring;

import io.cucumber.core.backend.*;
import io.cucumber.spring.annotationconfig.AnnotationContextConfiguration;
import io.cucumber.spring.cucumbercontextconfigannotation.AbstractWithComponentAnnotation;
import io.cucumber.spring.cucumbercontextconfigannotation.AnnotatedInterface;
import io.cucumber.spring.cucumbercontextconfigannotation.WithMetaAnnotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

class SpringBackendTest {

    private final Glue glue = new MockGlue();

    private MockObjectFactory factory;

    private SpringBackend backend;

    @BeforeEach
    void createBackend() {
        this.factory = new MockObjectFactory();
        this.backend = new SpringBackend(factory, currentThread()::getContextClassLoader);
    }

    @Test
    void finds_annotation_context_configuration_by_classpath_url() {
        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/spring/annotationconfig")));
        backend.buildWorld();
        assertTrue(factory.classes.contains(AnnotationContextConfiguration.class));
    }

    @Test
    void finds_annotaiton_context_configuration_once_by_classpath_url() {
        backend.loadGlue(glue, asList(
            URI.create("classpath:io/cucumber/spring/annotationconfig"),
            URI.create("classpath:io/cucumber/spring/annotationconfig")));
        backend.buildWorld();
        assertTrue(factory.classes.contains(AnnotationContextConfiguration.class));
        assertEquals(1, factory.classes.size());
    }

    @Test
    void ignoresAbstractClassWithCucumberContextConfiguration() {
        backend.loadGlue(glue, singletonList(
            URI.create("classpath:io/cucumber/spring/cucumbercontextconfigannotation")));
        backend.buildWorld();
        assertFalse(factory.classes.contains(AbstractWithComponentAnnotation.class));
    }

    @Test
    void ignoresInterfaceWithCucumberContextConfiguration() {
        backend.loadGlue(glue, singletonList(
            URI.create("classpath:io/cucumber/spring/cucumbercontextconfigannotation")));
        backend.buildWorld();
        assertFalse(factory.classes.contains(AnnotatedInterface.class));
    }

    @Test
    void considersClassWithCucumberContextConfigurationMetaAnnotation() {
        backend.loadGlue(glue, singletonList(
            URI.create("classpath:io/cucumber/spring/cucumbercontextconfigannotation")));
        backend.buildWorld();
        assertTrue(factory.classes.contains(WithMetaAnnotation.class));
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
