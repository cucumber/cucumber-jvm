package io.cucumber.java8;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.java8.steps.Steps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static java.lang.Thread.currentThread;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
class Java8BackendTest {

    @Mock
    private Glue glue;

    @Mock
    private ObjectFactory factory;

    private Java8Backend backend;

    @BeforeEach
    void createBackend() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        this.backend = new Java8Backend(factory, factory, resourceLoader);
    }

    @Test
    void finds_step_definitions_by_classpath_url() {
        backend.loadGlue(glue, singletonList(URI.create("classpath:io/cucumber/java8/steps")));
        backend.buildWorld();
        verify(factory).addClass(Steps.class);
    }

}
