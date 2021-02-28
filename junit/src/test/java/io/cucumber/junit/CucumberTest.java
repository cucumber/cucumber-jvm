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
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;

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
    void cucumber_can_run_features_in_parallel() throws Exception {
        RunNotifier notifier = new RunNotifier();
        RunListener listener = Mockito.mock(RunListener.class);
        notifier.addListener(listener);
        ParallelComputer computer = new ParallelComputer(true, true);
        Request.classes(computer, ValidEmpty.class).getRunner().run(notifier);
        {
            InOrder order = Mockito.inOrder(listener);

            order.verify(listener)
                    .testStarted(argThat(new DescriptionMatcher("Followed by some examples #1(Feature A)")));
            order.verify(listener)
                    .testFinished(argThat(new DescriptionMatcher("Followed by some examples #1(Feature A)")));
            order.verify(listener)
                    .testStarted(argThat(new DescriptionMatcher("Followed by some examples #2(Feature A)")));
            order.verify(listener)
                    .testFinished(argThat(new DescriptionMatcher("Followed by some examples #2(Feature A)")));
            order.verify(listener)
                    .testStarted(argThat(new DescriptionMatcher("Followed by some examples #3(Feature A)")));
            order.verify(listener)
                    .testFinished(argThat(new DescriptionMatcher("Followed by some examples #3(Feature A)")));
        }
        {
            InOrder order = Mockito.inOrder(listener);
            order.verify(listener).testStarted(argThat(new DescriptionMatcher("A(Feature B)")));
            order.verify(listener).testFinished(argThat(new DescriptionMatcher("A(Feature B)")));
            order.verify(listener).testStarted(argThat(new DescriptionMatcher("B(Feature B)")));
            order.verify(listener).testFinished(argThat(new DescriptionMatcher("B(Feature B)")));
            order.verify(listener).testStarted(argThat(new DescriptionMatcher("C #1(Feature B)")));
            order.verify(listener).testFinished(argThat(new DescriptionMatcher("C #1(Feature B)")));
            order.verify(listener).testStarted(argThat(new DescriptionMatcher("C #2(Feature B)")));
            order.verify(listener).testFinished(argThat(new DescriptionMatcher("C #2(Feature B)")));
            order.verify(listener).testStarted(argThat(new DescriptionMatcher("C #3(Feature B)")));
            order.verify(listener).testFinished(argThat(new DescriptionMatcher("C #3(Feature B)")));
        }
    }

    @Test
    void cucumber_distinguishes_between_identical_features() throws Exception {
        RunNotifier notifier = new RunNotifier();
        RunListener listener = Mockito.mock(RunListener.class);
        notifier.addListener(listener);
        Request.classes(ValidEmpty.class).getRunner().run(notifier);
        {
            InOrder order = Mockito.inOrder(listener);

            order.verify(listener)
                    .testStarted(
                        argThat(new DescriptionMatcher("A single scenario(A feature with a single scenario #1)")));
            order.verify(listener)
                    .testFinished(
                        argThat(new DescriptionMatcher("A single scenario(A feature with a single scenario #1)")));

            order.verify(listener)
                    .testStarted(
                        argThat(new DescriptionMatcher("A single scenario(A feature with a single scenario #2)")));
            order.verify(listener)
                    .testFinished(
                        argThat(new DescriptionMatcher("A single scenario(A feature with a single scenario #2)")));

        }
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

}
