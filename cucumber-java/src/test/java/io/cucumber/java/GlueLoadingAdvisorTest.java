package io.cucumber.java;

import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.resource.ClasspathSupport;
import io.cucumber.java.steps.Steps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class GlueLoadingAdvisorTest {

    final java.util.List<URI> gluePaths = List.of(URI.create("classpath:/com"));
    final TestClock clock = new TestClock(Instant.now(), ZoneId.systemDefault());
    final LogRecordListener listener = new LogRecordListener();

    @BeforeEach
    void setup() {
        LoggerFactory.addListener(listener);
        GlueLoadingAdvisor.loggedOnce.set(false);
    }

    @Test
    void logs_loadGlue_hints_default_options_class_without_glue() {
        RuntimeOptions options = RuntimeOptions.defaultOptions();
        GlueLoadingAdvisor advisor = new GlueLoadingAdvisor(options, clock);

        // When loading a lot of classes
        advisor.glueLoadingStarted();
        advisor.addGlueClass(GlueLoadingAdvisor.class);
        clock.tick(Duration.ofSeconds(1));
        advisor.logGlueLoadingSuggestions(gluePaths);

        // Then we log some hint message to improve the situation
        String message = listener.getLogRecords().get(0).getMessage();
        assertThat(message).startsWith("""
                Scanning the glue packages took 1000 ms for 1 classes, but only 0 of them contained Cucumber classes.
                You could gain about 1000 ms by more precisely specifying the glue package.
                """);
    }

    @Test
    void logs_loadGlue_hints_default_options_public_static_inner_classes() {
        RuntimeOptions options = RuntimeOptions.defaultOptions();
        GlueLoadingAdvisor advisor = new GlueLoadingAdvisor(options, clock);

        // When loading a lot of classes
        advisor.glueLoadingStarted();
        advisor.addGlueClass(Steps.class);
        advisor.addContainerClass(Steps.class);
        advisor.addGlueClass(PublicStaticInnerClass.class);
        clock.tick(Duration.ofSeconds(1));
        advisor.logGlueLoadingSuggestions(gluePaths);

        // Then we log some hint message to improve the situation
        String message = listener.getLogRecords().get(0).getMessage();
        assertThat(message).isEqualTo(
            """
                    Scanning the glue packages took 1000 ms for 2 classes, but only 1 of them contained Cucumber classes.
                    You could gain about 500 ms by more precisely specifying the glue package.

                    Some advice in order of decreasing efficiency:
                    1) for classes that contain steps/hooks/injectors, change public static inner classes to private (or remove them from the glue package), e.g.:
                    io.cucumber.java.GlueLoadingAdvisorTest$PublicStaticInnerClass
                    """);
    }

    @Test
    void logs_loadGlue_hints_default_options_non_public_static_inner_classes() {
        RuntimeOptions options = RuntimeOptions.defaultOptions();
        GlueLoadingAdvisor advisor = new GlueLoadingAdvisor(options, clock);

        // When loading a lot of classes
        advisor.glueLoadingStarted();
        advisor.addGlueClass(Steps.class);
        advisor.addContainerClass(Steps.class);
        advisor.addGlueClass(NonPublicStaticInnerClass.class);
        clock.tick(Duration.ofSeconds(1));
        advisor.logGlueLoadingSuggestions(gluePaths);

        // Then we log some hint message to improve the situation
        String message = listener.getLogRecords().get(0).getMessage();
        assertThat(message).isEqualTo(
            """
                    Scanning the glue packages took 1000 ms for 2 classes, but only 1 of them contained Cucumber classes.
                    You could gain about 500 ms by more precisely specifying the glue package.

                    Some advice in order of decreasing efficiency:
                    1) for classes that contain steps/hooks/injectors, remove non-public classes from the glue package, e.g.:
                    io.cucumber.java.GlueLoadingAdvisorTest$NonPublicStaticInnerClass
                    """);
    }

    @Test
    void logs_loadGlue_hints_default_options() {
        RuntimeOptions options = RuntimeOptions.defaultOptions();
        GlueLoadingAdvisor advisor = new GlueLoadingAdvisor(options, clock);

        // When loading a lot of classes
        advisor.glueLoadingStarted();
        advisor.addGlueClass(GlueLoadingAdvisor.class);
        advisor.addGlueClass(Steps.class);
        advisor.addContainerClass(Steps.class);
        clock.tick(Duration.ofSeconds(1));
        advisor.logGlueLoadingSuggestions(gluePaths);

        // Then we log some hint message to improve the situation
        String message = listener.getLogRecords().get(0).getMessage();
        assertThat(message).isEqualTo(
            """
                    Scanning the glue packages took 1000 ms for 2 classes, but only 1 of them contained Cucumber classes.
                    You could gain about 500 ms by more precisely specifying the glue package.

                    Some advice in order of decreasing efficiency:
                    1) remove the classes that do not contain cucumber step/hooks/injectors, e.g.:
                    io.cucumber.java.GlueLoadingAdvisor

                    2) for classes that contain steps/hooks/injectors, remove non-public classes from the glue package, e.g.:
                    io.cucumber.java.GlueLoadingAdvisor
                    """);
    }

    @Test
    void logs_loadGlue_hints_default_options_glue_only_classes() {
        RuntimeOptions options = RuntimeOptions.defaultOptions();
        GlueLoadingAdvisor advisor = new GlueLoadingAdvisor(options, clock);

        // When loading only classes with glue
        advisor.glueLoadingStarted();
        advisor.addGlueClass(Steps.class);
        advisor.addContainerClass(Steps.class);
        clock.tick(Duration.ofSeconds(1));
        advisor.logGlueLoadingSuggestions(gluePaths);

        // Then no hint is displayed
        assertThat(listener.getLogRecords()).isEmpty();
    }

    @Test
    void logs_loadGlue_hints_glue_path_is_default_package() {
        RuntimeOptions options = RuntimeOptions.defaultOptions();
        GlueLoadingAdvisor advisor = new GlueLoadingAdvisor(options, clock);

        // When loading classes from root package
        advisor.glueLoadingStarted();
        advisor.addGlueClass(GlueLoadingAdvisor.class);
        advisor.addGlueClass(Steps.class);
        advisor.addContainerClass(Steps.class);
        clock.tick(Duration.ofSeconds(1));
        advisor.logGlueLoadingSuggestions(singletonList(URI.create("classpath:/")));

        // Then the default hint is displayed
        String message = listener.getLogRecords().get(0).getMessage();
        assertThat(message).startsWith("""
                Scanning the glue packages took 1000 ms for 2 classes, but only 1 of them contained Cucumber classes.
                You could gain about 500 ms by more precisely specifying the glue package.

                Some advice in order of decreasing efficiency:
                1) %s
                """.formatted(ClasspathSupport.classPathScanningExplanation()));
    }

    @Test
    void logs_loadGlue_hints_no_display() {
        // Given glue loading hint is disabled
        RuntimeOptions options = new RuntimeOptionsBuilder().setGlueHintEnabled(false).build();
        GlueLoadingAdvisor advisor = new GlueLoadingAdvisor(options, clock);

        // When loading a lot of classes
        advisor.glueLoadingStarted();
        advisor.addGlueClass(GlueLoadingAdvisor.class);
        advisor.addGlueClass(Steps.class);
        advisor.addContainerClass(Steps.class);
        advisor.addGlueClass(PublicStaticInnerClass.class);
        clock.tick(Duration.ofSeconds(1));
        advisor.logGlueLoadingSuggestions(gluePaths);

        // Then no hint is displayed
        assertThat(listener.getLogRecords()).isEmpty();
    }

    @Test
    void logs_loadGlue_hints_no_glue_no_display() {
        RuntimeOptions options = RuntimeOptions.defaultOptions();
        GlueLoadingAdvisor advisor = new GlueLoadingAdvisor(options);

        // When loading no classes
        clock.tick(Duration.ofSeconds(1));
        advisor.logGlueLoadingSuggestions(gluePaths);

        // Then no hint is displayed
        assertThat(listener.getLogRecords()).isEmpty();
    }

    @Test
    void logs_loadGlue_hints_below_threshold() {
        RuntimeOptions options = new RuntimeOptionsBuilder().setGlueHintThreshold(Duration.ofMillis(201)).build();
        GlueLoadingAdvisor advisor = new GlueLoadingAdvisor(options, clock);

        advisor.glueLoadingStarted();
        advisor.addGlueClass(GlueLoadingAdvisor.class);
        advisor.addGlueClass(Steps.class);
        advisor.addContainerClass(Steps.class);
        advisor.addGlueClass(PublicStaticInnerClass.class);
        // When loading a lot of classes
        clock.tick(Duration.ofMillis(300));
        advisor.logGlueLoadingSuggestions(gluePaths);

        // Then no hint is displayed
        assertThat(listener.getLogRecords()).isEmpty();
    }

    public static class PublicStaticInnerClass {
    }

    static class NonPublicStaticInnerClass {
    }

}
