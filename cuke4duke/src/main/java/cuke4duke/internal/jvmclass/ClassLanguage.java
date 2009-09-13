package cuke4duke.internal.jvmclass;

import cuke4duke.internal.language.ProgrammingLanguage;
import cuke4duke.internal.language.LanguageMixin;
import cuke4duke.internal.language.Hook;
import cuke4duke.internal.java.JavaRegistrar;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class ClassLanguage extends ProgrammingLanguage {
    private final LanguageMixin languageMixin;
    private final ObjectFactory objectFactory;
    private final List<HookAndStepDefinitionRegistrar> registrars = new ArrayList<HookAndStepDefinitionRegistrar>();

    public ClassLanguage(LanguageMixin languageMixin) throws Throwable {
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

        registrars.add(new JavaRegistrar());
        // Add more registrars here - e.g. ScalaRegistrar
    }

    public void begin_scenario() {
        objectFactory.createObjects();
    }

    public void end_scenario() {
        objectFactory.disposeObjects();
    }


    protected void load(String classFile) throws Throwable {
        Class<?> clazz = loadClass(classFile);
        registerHooksAndStepDefinitionsFor(clazz);
        if(!Modifier.isAbstract(clazz.getModifiers())) {
            objectFactory.addClass(clazz);
        }
    }

    private void registerHooksAndStepDefinitionsFor(Class<?> clazz) throws Throwable {
        for(HookAndStepDefinitionRegistrar registrar : registrars) {
            registrar.registerHooksAndStepDefinitionsFor(clazz, this);
        }
    }

    private Class<?> loadClass(String classFile) throws ClassNotFoundException {
        String withoutExt = classFile.substring(0, classFile.length() - ".class".length());
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
        throw new ClassNotFoundException("Couldn't determine class from file: " + classFile);
    }

    public void addHook(String phase, Hook hook) {
        languageMixin.add_hook(phase, hook);
    }

    public Object getTarget(Class<?> type) {
        Object target = objectFactory.getComponent(type);
        if(target == null) {
            throw new NullPointerException("Couldn't find object for type " + type);
        }
        return target;
    }
}
