package cuke4duke.internal.java;

import cuke4duke.*;
import cuke4duke.internal.language.LanguageMixin;
import cuke4duke.internal.language.ProgrammingLanguage;
import cuke4duke.internal.language.StepDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public class JavaLanguage extends ProgrammingLanguage {
    private final ObjectFactory objectFactory;
    private final LanguageMixin languageMixin;

    @SuppressWarnings("unchecked")
	public JavaLanguage(LanguageMixin languageMixin) throws Throwable {
        this.languageMixin = languageMixin;
        String className = System.getProperty("cuke4duke.objectFactory");
        if(className == null) {
            throw new RuntimeException("Missing system property: cuke4duke.objectFactory");
        }
        Class<ObjectFactory> ofc = (Class<ObjectFactory>) Thread.currentThread().getContextClassLoader().loadClass(className);
        Constructor<ObjectFactory> ctor = ofc.getConstructor();
        try {
            objectFactory = ctor.newInstance();
        } catch(InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    protected void load(String java_file) throws ClassNotFoundException {
        Class<?> clazz = loadClass(java_file);
        registerStepDefinitionsFor(clazz);
        if(!Modifier.isAbstract(clazz.getModifiers())) {
            objectFactory.addClass(clazz);
        }
    }

    public void begin_scenario() {
        objectFactory.newWorld();
    }

    public void end_scenario() {
        objectFactory.dispose();
    }

    private Class<?> loadClass(String javaPath) throws ClassNotFoundException {
        String withoutExt = javaPath.substring(0, javaPath.length() - ".java".length());
        String[] pathElements = withoutExt.split("\\/");
        String className = null;
        for(int i = pathElements.length - 1; i >= 0; i--) {
            if(className == null) {
                className = pathElements[i];
            } else {
                className = pathElements[i] + "." + className;
            }
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException ignore) {
            }
        }
        throw new ClassNotFoundException("Couldn't determine class from file: " + javaPath);
    }

    private void registerStepDefinitionsFor(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            registerStepDefinitionMaybe(method);
            registerBeforeMaybe(method);
            registerAfterMaybe(method);
        }
    }

    private void registerStepDefinitionMaybe(Method method) {
        String regexpString = null;
        if (method.isAnnotationPresent(Given.class)) {
            regexpString = method.getAnnotation(Given.class).value();
        } else if (method.isAnnotationPresent(When.class)) {
            regexpString = method.getAnnotation(When.class).value();
        } else if (method.isAnnotationPresent(Then.class)) {
            regexpString = method.getAnnotation(Then.class).value();
        }
        if (regexpString != null) {
            StepDefinition stepDefinition = new JavaStepDefinition(this, method, regexpString);
            addStepDefinition(stepDefinition);
        }
    }

    private void registerBeforeMaybe(Method method) {
        if (method.isAnnotationPresent(Before.class)) {
            List<String> tagNames = Arrays.asList(method.getAnnotation(Before.class).value().split(","));
            languageMixin.add_hook("before", new JavaHook(tagNames, method, this));
        }
    }

    private void registerAfterMaybe(Method method) {
        if (method.isAnnotationPresent(After.class)) {
            List<String> tagNames = Arrays.asList(method.getAnnotation(After.class).value().split(","));
            languageMixin.add_hook("after", new JavaHook(tagNames, method, this));
        }
    }

    Object getTarget(Class<?> type) {
        Object target = objectFactory.getComponent(type);
        if(target == null) {
            throw new NullPointerException("Couldn't find object for type " + type);
        }
        return target;
    }
}
