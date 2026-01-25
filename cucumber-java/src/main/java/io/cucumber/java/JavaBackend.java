package io.cucumber.java;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Options;
import io.cucumber.core.backend.Snippet;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.resource.ClasspathScanner;
import io.cucumber.core.resource.ClasspathSupport;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME;
import static io.cucumber.java.MethodScanner.scan;

final class JavaBackend implements Backend {
    private static final Logger log = LoggerFactory.getLogger(JavaBackend.class);

    private final Lookup lookup;
    private final Container container;
    private final ClasspathScanner classFinder;
    private final Options options;

    JavaBackend(Lookup lookup, Container container, Supplier<ClassLoader> classLoaderSupplier, Options options) {
        this.lookup = lookup;
        this.container = container;
        this.classFinder = new ClasspathScanner(classLoaderSupplier);
        this.options = options;
    }

    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        GlueAdaptor glueAdaptor = new GlueAdaptor(lookup, glue);

        final Set<Class<?>> containerClasses = new HashSet<>();
        final Set<Class<?>> glueClasses = new HashSet<>();

        long t0 = System.currentTimeMillis();
        gluePaths.stream()
                .filter(gluePath -> CLASSPATH_SCHEME.equals(gluePath.getScheme()))
                .map(ClasspathSupport::packageName)
                .map(classFinder::scanForClassesInPackage)
                .flatMap(Collection::stream)
                .distinct()
                .forEach(aGlueClass -> {
                    glueClasses.add(aGlueClass);
                    scan(aGlueClass, (method, annotation) -> {
                        containerClasses.add(method.getDeclaringClass());
                        container.addClass(method.getDeclaringClass());
                        glueAdaptor.addDefinition(method, annotation);
                    });
                });

        if (options.isGlueHintEnabled()) {
            int glueClassCount = glueClasses.size();
            if (glueClassCount > 0) {
                long t1 = System.currentTimeMillis();
                long duration = t1 -
                        t0;
                int containerClassCount = containerClasses.size();
                long expectedGain = duration - duration * containerClassCount /
                        glueClassCount;
                if (expectedGain > options.getGlueHintThreshold()) {
                    List<String> suggestions = new ArrayList<>();
                    if (gluePaths.contains(URI.create("classpath:/"))) {
                        suggestions.add("1) " + ClasspathSupport.classPathScanningExplanation());
                    }

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

                    String nonPublicInnerClassesSuggestion = glueClasses.stream()
                            .filter(clazz -> !containerClasses.contains(clazz) &&
                                    !Modifier.isPublic(clazz.getModifiers()) &&
                                    clazz.getDeclaringClass() != null)
                            .limit(10)
                            .map(Class::getName)
                            .collect(Collectors.joining("\n"));
                    if (!nonPublicInnerClassesSuggestion.isEmpty()) {
                        suggestions.add((suggestions.size() + 1)
                                + ") for classes that contain steps/hooks/injectors, remove non-public static inner classes from the glue package, e.g.:\n"
                                +
                                nonPublicInnerClassesSuggestion + "\n");
                    }

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

    @Override
    public void buildWorld() {

    }

    @Override
    public void disposeWorld() {

    }

    @Override
    public Snippet getSnippet() {
        return new JavaSnippet();
    }

}
