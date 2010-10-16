package cucumber.runtime.java;

import cuke4duke.annotation.I18n;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class ReflectionsMethodFinder implements MethodFinder {
    private Reflections reflections;

    public ReflectionsMethodFinder(String stepDefPackage) {
        reflections = new Reflections(configuration(stepDefPackage));
    }

    public Set<Method> getStepDefinitionMethods() {
        return reflections.getMethodsAnnotatedWith(I18n.EN.Given.class);
    }

    private Configuration configuration(String stepDefPackage) {
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
                .setScanners(new MethodAnnotationsScanner())
                .filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(stepDefPackage)))
                .setUrls(stepDefUrls);
    }
}
