package io.cucumber.junit;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.gherkin.FeatureParserException;
import org.junit.experimental.ParallelComputer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CucumberTest {

    private String dir;

    @BeforeEach
    void ensureDirectory() {
        dir = System.getProperty("user.dir");
        if (dir.endsWith("cucumber-jvm")) {
            // Might be the case if we're running in an IDE - at least in IDEA.
            System.setProperty("user.dir", new File(dir, "junit").getAbsolutePath());
        }
    }

    @AfterEach
    void ensureOriginalDirectory() {
        System.setProperty("user.dir", dir);
    }

    @Test
    void finds_features_based_on_implicit_package() throws InitializationError {
        Cucumber cucumber = new Cucumber(ImplicitFeatureAndGluePath.class);
        assertThat(cucumber.getChildren().size(), is(equalTo(7)));
        assertThat(cucumber.getChildren().get(1).getDescription().getDisplayName(), is(equalTo("Feature A")));
    }

    @Test
    void finds_features_based_on_explicit_root_package() throws InitializationError {
        Cucumber cucumber = new Cucumber(ExplicitFeaturePath.class);
        assertThat(cucumber.getChildren().size(), is(equalTo(7)));
        assertThat(cucumber.getChildren().get(1).getDescription().getDisplayName(), is(equalTo("Feature A")));
    }

    @Test
    void testThatParsingErrorsIsNicelyReported() {
        Executable testMethod = () -> new Cucumber(LexerErrorFeature.class);
        FeatureParserException actualThrown = assertThrows(FeatureParserException.class, testMethod);
        assertThat(
            actualThrown.getMessage(),
            equalTo("" +
                    "Failed to parse resource at: classpath:io/cucumber/error/lexer_error.feature\n" +
                    "(1:1): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'Feature  FA'\n" +
                    "(3:3): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'Scenario SA'\n" +
                    "(4:5): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'Given GA'\n" +
                    "(5:5): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'When GA'\n" +
                    "(6:5): expected: #EOF, #Language, #TagLine, #FeatureLine, #Comment, #Empty, got 'Then TA'"));
    }

    @Test
    void testThatFileIsNotCreatedOnParsingError() {
        assertThrows(FeatureParserException.class,
            () -> new Cucumber(FormatterWithLexerErrorFeature.class));
        assertFalse(
            new File("target/lexor_error_feature.ndjson").exists(),
            "File is created despite Lexor Error");
    }

    @Test
    void finds_no_features_when_explicit_feature_path_has_no_features() throws InitializationError {
        Cucumber cucumber = new Cucumber(ExplicitFeaturePathWithNoFeatures.class);
        List<ParentRunner<?>> children = cucumber.getChildren();
        assertThat(children, is(equalTo(emptyList())));
    }

    @Test
    void cucumber_can_run_features_in_parallel() {
        RunNotifier notifier = new RunNotifier();
        MockRunListener listener = new MockRunListener();
        notifier.addListener(listener);
        ParallelComputer computer = new ParallelComputer(true, true);
        Request.classes(computer, ValidEmpty.class).getRunner().run(notifier);
        {
            List<RunListenerEvent> events = listener.events.stream()
                    .filter(runListenerEvent -> runListenerEvent.description.startsWith("Followed by some examples"))
                    .collect(Collectors.toList());
            assertIterableEquals(List.of(
                new RunListenerEvent("testStarted", "Followed by some examples #1(Feature A)"),
                new RunListenerEvent("testFinished", "Followed by some examples #1(Feature A)"),
                new RunListenerEvent("testStarted", "Followed by some examples #2(Feature A)"),
                new RunListenerEvent("testFinished", "Followed by some examples #2(Feature A)"),
                new RunListenerEvent("testStarted", "Followed by some examples #3(Feature A)"),
                new RunListenerEvent("testFinished", "Followed by some examples #3(Feature A)")), events);
        }
        {
            List<RunListenerEvent> events = listener.events.stream()
                    .filter(runListenerEvent -> runListenerEvent.description.endsWith("(Feature B)"))
                    .collect(Collectors.toList());
            assertIterableEquals(List.of(
                new RunListenerEvent("testStarted", "A(Feature B)"),
                new RunListenerEvent("testFinished", "A(Feature B)"),
                new RunListenerEvent("testStarted", "B(Feature B)"),
                new RunListenerEvent("testFinished", "B(Feature B)"),
                new RunListenerEvent("testStarted", "C #1(Feature B)"),
                new RunListenerEvent("testFinished", "C #1(Feature B)"),
                new RunListenerEvent("testStarted", "C #2(Feature B)"),
                new RunListenerEvent("testFinished", "C #2(Feature B)"),
                new RunListenerEvent("testStarted", "C #3(Feature B)"),
                new RunListenerEvent("testFinished", "C #3(Feature B)")), events);
        }
    }

    @Test
    void cucumber_distinguishes_between_identical_features() {
        RunNotifier notifier = new RunNotifier();
        MockRunListener listener = new MockRunListener();
        notifier.addListener(listener);
        Request.classes(ValidEmpty.class).getRunner().run(notifier);

        List<RunListenerEvent> events = listener.events.stream()
                .filter(runListenerEvent -> runListenerEvent.description.startsWith("A single scenario"))
                .collect(Collectors.toList());
        assertIterableEquals(List.of(
            new RunListenerEvent("testStarted", "A single scenario(A feature with a single scenario #1)"),
            new RunListenerEvent("testFinished", "A single scenario(A feature with a single scenario #1)"),
            new RunListenerEvent("testStarted", "A single scenario(A feature with a single scenario #2)"),
            new RunListenerEvent("testFinished", "A single scenario(A feature with a single scenario #2)")), events);
    }

    @Test
    void cucumber_returns_description_tree_with_features_and_pickles() throws InitializationError {
        Description description = new Cucumber(ValidEmpty.class).getDescription();

        assertThat(description.getDisplayName(), is("io.cucumber.junit.CucumberTest$ValidEmpty"));
        Description feature = description.getChildren().get(1);
        assertThat(feature.getDisplayName(), is("Feature A"));
        Description pickle = feature.getChildren().get(0);
        assertThat(pickle.getDisplayName(), is("A good start(Feature A)"));
    }

    @Test
    void no_stepdefs_in_cucumber_runner_valid() {
        Assertions.assertNoCucumberAnnotatedMethods(ValidEmpty.class);
        Assertions.assertNoCucumberAnnotatedMethods(ValidIgnored.class);
    }

    @Test
    void no_stepdefs_in_cucumber_runner_invalid() {
        Executable testMethod = () -> Assertions.assertNoCucumberAnnotatedMethods(Invalid.class);
        CucumberException expectedThrown = assertThrows(CucumberException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo(
            "\n\nClasses annotated with @RunWith(Cucumber.class) must not define any\nStep Definition or Hook methods. Their sole purpose is to serve as\nan entry point for JUnit. Step Definitions and Hooks should be defined\nin their own classes. This allows them to be reused across features.\nOffending class: class io.cucumber.junit.CucumberTest$Invalid\n")));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface DummyWhen {

    }

    @RunWith(Cucumber.class)
    public static class ValidEmpty {

    }

    @RunWith(Cucumber.class)
    public static class ValidIgnored {

        public void ignoreMe() {
        }

    }

    @RunWith(Cucumber.class)
    private static class Invalid {

        @DummyWhen
        public void ignoreMe() {
        }

    }

    @SuppressWarnings("WeakerAccess")
    public static class ImplicitFeatureAndGluePath {

    }

    @SuppressWarnings("WeakerAccess")
    @CucumberOptions(features = "classpath:io/cucumber/junit")
    public static class ExplicitFeaturePath {

    }

    @SuppressWarnings("WeakerAccess")
    @CucumberOptions(features = "classpath:gibber/ish")
    public static class ExplicitFeaturePathWithNoFeatures {

    }

    @SuppressWarnings("WeakerAccess")
    @CucumberOptions(features = "classpath:io/cucumber/error/lexer_error.feature")
    public static class LexerErrorFeature {

    }

    @SuppressWarnings("WeakerAccess")
    @CucumberOptions(
            features = "classpath:io/cucumber/error/lexer_error.feature",
            plugin = "message:target/lexor_error_feature.ndjson")
    public static class FormatterWithLexerErrorFeature {

    }

    private static class MockRunListener extends RunListener {
        List<RunListenerEvent> events = new ArrayList<>();
        @Override
        public void testStarted(Description description) {
            this.events.add(new RunListenerEvent("testStarted", description.getDisplayName()));
        }

        @Override
        public void testFinished(Description description) {
            this.events.add(new RunListenerEvent("testFinished", description.getDisplayName()));
        }

    }

    public static class RunListenerEvent {
        String eventName;
        String description;
        public RunListenerEvent(String eventName, String description) {
            this.eventName = eventName;
            this.description = description;
        }

        public boolean equals(Object o) {
            if (o instanceof RunListenerEvent) {
                return this.toString().equals(o.toString());
            } else {
                return false;
            }
        }

        public String toString() {
            return eventName + ", description=" + description;
        }

    }
}
