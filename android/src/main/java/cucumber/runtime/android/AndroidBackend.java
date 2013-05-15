package cucumber.runtime.android;

import android.app.Instrumentation;
import android.content.Context;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.runtime.*;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

public class AndroidBackend implements Backend {
    private final SnippetGenerator mSnippetGenerator = new SnippetGenerator(new JavaSnippet());
    private final ObjectFactory mObjectFactory;
    private final AndroidClasspathMethodScanner mClasspathMethodScanner;
    private Glue mGlue;

    public AndroidBackend(Instrumentation instrumentation) {
        mClasspathMethodScanner = new AndroidClasspathMethodScanner(instrumentation.getContext());
        mObjectFactory = new AndroidObjectFactory(instrumentation);
    }

    @Override
    public void loadGlue(Glue glue, List<String> gluePaths) {
        mGlue = glue;
        mClasspathMethodScanner.scan(this, gluePaths);
    }

    /**
     * Convenience method for frameworks that wish to load glue from methods explicitly (possibly
     * found with a different mechanism than Cucumber's built-in classpath scanning).
     *
     * @param glue          where stepdefs and hooks will be added.
     * @param method        a candidate method.
     * @param glueCodeClass the class implementing the method. Must not be a subclass of the class implementing the method.
     */
    public void loadGlue(Glue glue, Method method, Class<?> glueCodeClass) {
        mGlue = glue;
        mClasspathMethodScanner.scan(this, method, glueCodeClass);
    }

    @Override
    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
    }

    @Override
    public void buildWorld() {
        mObjectFactory.start();
    }

    @Override
    public void disposeWorld() {
        mObjectFactory.stop();
    }

    @Override
    public String getSnippet(Step step) {
        return mSnippetGenerator.getSnippet(step);
    }

    void addStepDefinition(Annotation annotation, Method method) {
        try {
            mObjectFactory.addClass(method.getDeclaringClass());
            mGlue.addStepDefinition(new AndroidJavaStepDefinition(
                    method,
                    pattern(annotation),
                    timeout(annotation),
                    mObjectFactory
            ));
        } catch (DuplicateStepDefinitionException e) {
            throw e;
        } catch (Throwable e) {
            throw new CucumberException(e);
        }
    }

    private Pattern pattern(Annotation annotation) throws Throwable {
        Method regexpMethod = annotation.getClass().getMethod("value");
        String regexpString = (String) Utils.invoke(annotation, regexpMethod, 0);
        return Pattern.compile(regexpString);
    }

    private int timeout(Annotation annotation) throws Throwable {
        Method regexpMethod = annotation.getClass().getMethod("timeout");
        return (Integer) Utils.invoke(annotation, regexpMethod, 0);
    }

    void addHook(Annotation annotation, Method method) {
        mObjectFactory.addClass(method.getDeclaringClass());
        if (annotation.annotationType().equals(Before.class)) {
            String[] tagExpressions = ((Before) annotation).value();
            int timeout = ((Before) annotation).timeout();
            mGlue.addBeforeHook(new JavaHookDefinition(method, tagExpressions, ((Before) annotation).order(), timeout, mObjectFactory));
        } else {
            String[] tagExpressions = ((After) annotation).value();
            int timeout = ((After) annotation).timeout();
            mGlue.addAfterHook(new JavaHookDefinition(method, tagExpressions, ((After) annotation).order(), timeout, mObjectFactory));
        }
    }
}
