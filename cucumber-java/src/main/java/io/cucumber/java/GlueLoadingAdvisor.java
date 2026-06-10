package io.cucumber.java;

import io.cucumber.core.backend.Options;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.options.Constants;
import io.cucumber.core.resource.ClasspathSupport;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class gives advices to the developer to improve the glue loading
 * performance.
 */
final class GlueLoadingAdvisor {
    private static final Logger log = LoggerFactory.getLogger(GlueLoadingAdvisor.class);
    private final Set<Class<?>> containerClasses = new HashSet<>();
    private final Set<Class<?>> glueClasses = new HashSet<>();
    private final Options options;
    private final Clock clock;
    private @Nullable Instant glueLoadingStart;

    GlueLoadingAdvisor(Options options) {
        this(options, Clock.systemUTC());
    }

    GlueLoadingAdvisor(Options options, Clock clock) {
        this.options = options;
        this.clock = clock;
    }

    /**
     * Logs suggestions to improve the glue loading performance if the scanning
     * of glue package takes more time than the threshold defined by the
     * {@value Constants#GLUE_HINT_THRESHOLD_PROPERTY_NAME} property. Enabled by
     * {@value Constants#GLUE_HINT_ENABLED_PROPERTY_NAME} property. The
     * suggestions are ordered by decreasing efficiency.
     *
     * @param gluePaths the glue paths that have been scanned for glue classes.
     */
    void logGlueLoadingSuggestions(List<URI> gluePaths) {
        if (!options.isGlueHintEnabled()) {
            return;
        }
        int glueClassCount = glueClasses.size();
        if (glueClassCount == 0) {
            return;
        }
        if (glueLoadingStart == null) {
            return;
        }
        var duration = Duration.between(glueLoadingStart, Instant.now(clock));
        if (duration.isNegative()) {
            return;
        }

        int containerClassCount = containerClasses.size();
        Duration durationPerGlueClass = duration.multipliedBy(containerClassCount).dividedBy(glueClassCount);
        var expectedGain = duration.minus(durationPerGlueClass);

        if (expectedGain.compareTo(options.getGlueHintThreshold()) < 0) {
            return;
        }

        List<String> suggestions = new ArrayList<>();

        // TODO suggests to use "cucumber.glue-classes" property
        // from https://github.com/cucumber/cucumber-jvm/pull/3120
        addSuggestionCucumberGlue(gluePaths, suggestions);
        addSuggestionRemoveClassWithoutGlueFromGluePackage(suggestions);
        addSuggestionChangePublicStaticInnerClassesToPrivateClasses(suggestions);
        addSuggestionRemoveNonPublicClassFromGluePackage(suggestions);

        log.info(() -> """
                Scanning the glue packages took %s ms for %d classes, but only %d of them contained Cucumber classes.
                You could gain about %s ms by more precisely specifying the glue package.

                Some advice in order of decreasing efficiency:
                %s""".formatted(duration.toMillis(), glueClassCount, containerClassCount, expectedGain.toMillis(),
            String.join("\n", suggestions)));
    }

    private static void addSuggestionCucumberGlue(List<URI> gluePaths, List<String> suggestions) {
        if (gluePaths.contains(URI.create("classpath:/"))) {
            suggestions.add("1) " + ClasspathSupport.classPathScanningExplanation());
        }
    }

    private void addSuggestionRemoveClassWithoutGlueFromGluePackage(List<String> suggestions) {
        String classesNotContainingGlueSuggestion = glueClasses.stream()
                .filter(clazz -> !containerClasses.contains(clazz) && clazz.getDeclaringClass() == null)
                .limit(10)
                .map(Class::getName)
                .collect(Collectors.joining("\n"));
        if (!classesNotContainingGlueSuggestion.isEmpty()) {
            suggestions.add((suggestions.size() + 1)
                    + ") remove the classes that do not contain cucumber step/hooks/injectors, e.g.:\n" +
                    classesNotContainingGlueSuggestion + "\n");
        }
    }

    private void addSuggestionChangePublicStaticInnerClassesToPrivateClasses(List<String> suggestions) {
        String publicInnerClassesSuggestion = glueClasses.stream()
                .filter(clazz -> !containerClasses.contains(clazz) &&
                        Modifier.isPublic(clazz.getModifiers()) &&
                        clazz.getDeclaringClass() != null)
                .limit(10)
                .map(Class::getName)
                .collect(Collectors.joining("\n"));
        if (!publicInnerClassesSuggestion.isEmpty()) {
            suggestions.add((suggestions.size() + 1)
                    + ") for classes that contain steps/hooks/injectors, change public static inner classes to private (or remove them from the glue package), e.g.:\n"
                    +
                    publicInnerClassesSuggestion + "\n");
        }
    }

    private void addSuggestionRemoveNonPublicClassFromGluePackage(List<String> suggestions) {
        String nonPublicInnerClassesSuggestion = glueClasses.stream()
                .filter(clazz -> !containerClasses.contains(clazz) &&
                        !Modifier.isPublic(clazz.getModifiers()))
                .limit(10)
                .map(Class::getName)
                .collect(Collectors.joining("\n"));
        if (!nonPublicInnerClassesSuggestion.isEmpty()) {
            suggestions.add((suggestions.size() + 1)
                    + ") for classes that contain steps/hooks/injectors, remove non-public classes from the glue package, e.g.:\n"
                    +
                    nonPublicInnerClassesSuggestion + "\n");
        }
    }

    void glueLoadingStarted() {
        this.glueLoadingStart = Instant.now(clock);
    }

    /**
     * Adds a class coming from the glue package. It may or may not contain glue
     * items (step definitions, hooks, injectors).
     *
     * @param glueClass the class coming from the glue package
     */
    void addGlueClass(Class<?> glueClass) {
        glueClasses.add(glueClass);
    }

    /**
     * Adds a class containing glue items (step definitions, hooks, injectors).
     *
     * @param containerClass the class that have been added to the container
     */
    void addContainerClass(Class<?> containerClass) {
        containerClasses.add(containerClass);
    }
}
