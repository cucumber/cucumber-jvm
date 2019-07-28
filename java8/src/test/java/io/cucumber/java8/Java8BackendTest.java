package io.cucumber.java8;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.java8.stepdefs.Stepdefs;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.net.URI;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

public class Java8BackendTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Glue glue;

    @Mock
    private ObjectFactory factory;

    private Java8Backend backend;

    @Before
    public void createBackend() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        this.backend = new Java8Backend(factory, factory, resourceLoader);
    }

    @Test
    public void finds_step_definitions_by_classpath_url() {
        backend.loadGlue(glue, asList(URI.create("classpath:io/cucumber/java8/steps")));
        backend.buildWorld();
        verify(factory).addClass(Stepdefs.class);
    }

}
