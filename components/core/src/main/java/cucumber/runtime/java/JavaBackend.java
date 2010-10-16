package cucumber.runtime.java;

import cucumber.StepDefinition;
import cucumber.runtime.Backend;
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
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class JavaBackend implements Backend {
    private final ObjectFactory objectFactory;
    private final String stepDefPackage;

    public JavaBackend(ObjectFactory objectFactory, String stepDefPackage) {
        this.objectFactory = objectFactory;
        this.stepDefPackage = stepDefPackage;
    }

    public List<StepDefinition> getStepDefinitions() {
        Reflections reflections = new Reflections(configuration());
        Set<Method> methods = reflections.getMethodsAnnotatedWith(I18n.EN.Given.class);
        List<StepDefinition> result = new ArrayList<StepDefinition>();
        for(Method method : methods) {
            objectFactory.addClass(method.getDeclaringClass());
            Pattern pattern = Pattern.compile(method.getAnnotation(I18n.EN.Given.class).value());
            result.add(new MethodStepDefinition(pattern, method, objectFactory));
        }

        return result;
    }

    public void newScenario() {
        objectFactory.createObjects();
    }

    private Configuration configuration() {
        Collection<URL> stepDefUrls = new ArrayList<URL>();
        String javaClassPath = System.getProperty("java.class.path");
        if (javaClassPath != null) {
            for (String path : javaClassPath.split(File.pathSeparator)) {
                File file = new File(path);
                if(file.isDirectory()) {
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
