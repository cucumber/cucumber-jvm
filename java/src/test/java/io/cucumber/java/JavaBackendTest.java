package io.cucumber.java;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.java.stepdefs.Stepdefs;
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

public class JavaBackendTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Captor
    public ArgumentCaptor<StepDefinition> stepDefinition;

    @Mock
    private Glue glue;

    @Mock
    private ObjectFactory factory;

    private JavaBackend backend;

    @Before
    public void createBackend() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);
        this.backend = new JavaBackend(factory, resourceLoader, typeRegistry);
    }

    @Test
    public void finds_step_definitions_by_classpath_url() {
        backend.loadGlue(glue, asList("classpath:io/cucumber/java/stepdefs"));
        backend.buildWorld();
        verify(factory).addClass(Stepdefs.class);
    }

    @Test
    public void finds_step_definitions_by_package_name() {
        backend.loadGlue(glue, asList("io.cucumber.java.stepdefs"));
        backend.buildWorld();
        verify(factory).addClass(Stepdefs.class);
    }

    @Test
    public void detects_subclassed_glue_and_throws_exception() {
        final Executable testMethod = () -> backend.loadGlue(glue, asList("io.cucumber.java.stepdefs", "io.cucumber.java.incorrectlysubclassedstepdefs"));
        final CucumberException expectedThrown = assertThrows(CucumberException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("You're not allowed to extend classes that define Step Definitions or hooks. class io.cucumber.java.incorrectlysubclassedstepdefs.SubclassesStepdefs extends class io.cucumber.java.stepdefs.Stepdefs")));
    }

    @Test
    public void detects_repeated_annotations() {
        backend.loadGlue(glue, asList("io.cucumber.java.repeatable"));
        verify(glue, times(2)).addStepDefinition(stepDefinition.capture());

        List<String> patterns = stepDefinition.getAllValues()
            .stream()
            .map(StepDefinition::getPattern)
            .collect(toList());
        assertThat(patterns, equalTo(asList("test", "test again")));

    }

}
