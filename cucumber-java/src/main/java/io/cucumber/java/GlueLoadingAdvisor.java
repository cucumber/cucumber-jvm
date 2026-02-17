package io.cucumber.java;

import io.cucumber.core.backend.Options;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.resource.ClasspathSupport;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class gives advices to the developer to improve the glue loading
 * performance.
 */
public final class GlueLoadingAdvisor {
    private static final Logger log = LoggerFactory.getLogger(GlueLoadingAdvisor.class);
    final Set<Class<?>> containerClasses = new HashSet<>();
    final Set<Class<?>> glueClasses = new HashSet<>();
    final Options options;
    long t0 = System.currentTimeMillis();

    public GlueLoadingAdvisor(Options options) {
        this.options = options;
    }

    /**
     * Logs advices to improve the glue loading performance if the scanning of
     * glue package takes more time than the threshold defined in
     * "cucumber.glue.hint.threshold" property. Enabled by
     * "cucumber.glue.hint.enabled" property. The advices are ordered by
     * decreasing efficiency.
     * 
     * @param gluePaths the glue paths that have been scanned for glue classes.
     */
    public void logGlueLoadingAdvices(List<URI> gluePaths) {
        if (options.isGlueHintEnabled()) {
            int glueClassCount = glueClasses.size();
            if (glueClassCount > 0) {
                long t1 = System.currentTimeMillis();
                long duration = t1 - t0;
                int containerClassCount = containerClasses.size();
                long expectedGain = duration - duration * containerClassCount / glueClassCount;
                if (expectedGain > options.getGlueHintThreshold()) {
                    List<String> suggestions = new ArrayList<>();

                    // TODO suggests to use "cucumber.glue-classes" property
                    // from https://github.com/cucumber/cucumber-jvm/pull/3120
                    addSuggestionCucumberGlue(gluePaths, suggestions);
                    addSuggestionRemoveClassWithoutGlueFromGluePackage(suggestions);
                    addSuggestionChangePublicStaticInnerClassesToPrivateClasses(suggestions);
                    addSuggestionRemoveNonPublicClassFromGluePackage(suggestions);

                    log.info(() -> "Scanning the glue packages took " + duration +
                            " ms for " + glueClassCount + " classes, but only " +
                            containerClassCount +
                            " of them are Cucumber glue items. You could gain about " +
                            expectedGain +
                            " ms by cleaning the glue package. Some advices (by decreasing efficiency):\n" +
                            String.join("\n", suggestions));
                }
            }
        }
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

    /**
     * Adds a class coming from the glue package. It may or may not contain glue
     * items (step definitions, hooks, injectors).
     * 
     * @param glueClass the class coming from the glue package
     */
    public void addGlueClass(Class<?> glueClass) {
        glueClasses.add(glueClass);
    }

    /**
     * Adds a class containing glue items (step definitions, hooks, injectors).
     * 
     * @param containerClass the class that have been added to the container
     */
    public void addContainerClass(Class<?> containerClass) {
        containerClasses.add(containerClass);
    }
}
