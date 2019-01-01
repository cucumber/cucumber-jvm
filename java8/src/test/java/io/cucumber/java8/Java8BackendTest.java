package io.cucumber.java8;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.java8.stepdefs.Stepdefs;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;
import java.util.Locale;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class Java8BackendTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Captor
    public ArgumentCaptor<StepDefinition> stepDefinition;

    @Mock
    private Glue glue;

    @Mock
    private ObjectFactory factory;

    private Java8Backend backend;

    @Before
    public void createBackend() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);
        this.backend = new Java8Backend(factory, resourceLoader, typeRegistry);
    }

    @Test
    public void finds_step_definitions_by_classpath_url() {
        backend.loadGlue(glue, asList("classpath:io/cucumber/java8/stepdefs"));
        backend.buildWorld();
        verify(factory).addClass(Stepdefs.class);
    }

    @Test
    public void finds_step_definitions_by_package_name() {
        backend.loadGlue(glue, asList("io.cucumber.java8.stepdefs"));
        backend.buildWorld();
        verify(factory).addClass(Stepdefs.class);
    }

}
