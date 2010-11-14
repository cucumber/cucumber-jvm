package cucumber.runtime.java.reflections;

import cucumber.runtime.java.MethodFinder;
import cuke4duke.internal.java.annotation.CucumberAnnotation;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReflectionsMethodFinder implements MethodFinder {
    private final Reflections reflections;
    private final Reflections cucumberReflections;

    public ReflectionsMethodFinder(String stepDefPackage) {
        reflections = new Reflections(configuration(stepDefPackage, new MethodAnnotationsScanner()));
        cucumberReflections = new Reflections(configuration("cuke4duke.annotation", new TypeAnnotationsScanner()));
    }

    public Set<Method> getStepDefinitionMethods() {
        Set<Class<?>> annotations = cucumberReflections.getTypesAnnotatedWith(CucumberAnnotation.class);
        Set<Method> methods = new HashSet<Method>();
        for (Class<?> annotation : annotations) {
            methods.addAll(reflections.getMethodsAnnotatedWith((Class<? extends Annotation>) annotation));
        }
        return methods;
    }

    private Configuration configuration(String stepDefPackage, Scanner scanner) {
        Collection<URL> stepDefUrls = new ArrayList<URL>();
        String javaClassPath = System.getProperty("java.class.path");
        if (javaClassPath != null) {
            for (String path : javaClassPath.split(File.pathSeparator)) {
                File file = new File(path);
                if (file.isDirectory()) {
                    try {
                        stepDefUrls.add(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new InternalError("Can't convert to URL: " + path);
                    }
                }
            }
        }
        return new ConfigurationBuilder()
                .setScanners(scanner)
                .filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(stepDefPackage)))
                .setUrls(stepDefUrls);
    }
}
