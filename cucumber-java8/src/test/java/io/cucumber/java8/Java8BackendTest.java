package io.cucumber.java8;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.GlueDiscoveryRequest;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.java8.steps.Steps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import static io.cucumber.core.backend.GlueDiscoverySelector.selectUri;
import static java.lang.Thread.currentThread;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@MockitoSettings
class Java8BackendTest {

    @Mock
    private Glue glue;

    @Mock
    private ObjectFactory factory;

    private Java8Backend backend;

    @BeforeEach
    void createBackend() {
        this.backend = new Java8Backend(factory, factory, currentThread()::getContextClassLoader);
    }

    @Test
    void finds_step_definitions_by_classpath_url() {
        var request = GlueDiscoveryRequest.builder() //
                .selectors(selectUri("classpath:io/cucumber/java8/steps")) //
                .build();
        backend.loadGlue(glue, request);
        backend.buildWorld();
        verify(factory).addClass(Steps.class);
    }

    @Test
    void finds_step_definitions_once_by_classpath_url() {
        var request = GlueDiscoveryRequest.builder() //
                .selectors(selectUri("classpath:io/cucumber/java8/steps")) //
                .selectors(selectUri("classpath:io/cucumber/java8/steps")) //
                .build();
        backend.loadGlue(glue, request);
        backend.buildWorld();
        verify(factory, times(1)).addClass(Steps.class);
    }

}
