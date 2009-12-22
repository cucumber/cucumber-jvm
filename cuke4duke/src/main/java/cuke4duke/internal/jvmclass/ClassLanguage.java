package cuke4duke.internal.jvmclass;

import cuke4duke.StepMother;
import cuke4duke.internal.JRuby;
import cuke4duke.internal.language.AbstractProgrammingLanguage;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public class ClassLanguage extends AbstractProgrammingLanguage {
    private final ObjectFactory objectFactory;
    private final List<ClassAnalyzer> analyzers;
    private Collection<Class<?>> classes = new ArrayList<Class<?>>();

    public ClassLanguage(ClassLanguageMixin languageMixin, StepMother stepMother, List<ClassAnalyzer> analyzers) throws Throwable {
        this(languageMixin, stepMother, analyzers, createObjectFactory());
    }

    public ClassLanguage(ClassLanguageMixin languageMixin, StepMother stepMother, List<ClassAnalyzer> analyzers, ObjectFactory objectFactory) throws Throwable {
        super(languageMixin);
        this.analyzers = analyzers;
        this.objectFactory = objectFactory;
        objectFactory.addStepMother(stepMother);
        for (ClassAnalyzer analyzer : analyzers) {
            for (Class<?> clazz : analyzer.alwaysLoad()) {
                objectFactory.addClass(clazz);
            }
        }
    }

    @Override
    public void load_code_file(String classFile) throws Throwable {
        Class<?> clazz = loadClass(classFile);
        addClass(clazz);
    }

    public void addClass(Class<?> clazz) {
        if (!Modifier.isAbstract(clazz.getModifiers())) {
            objectFactory.addClass(clazz);
        }
        classes.add(clazz);
    }

    public Collection<Class<?>> getClasses() {
        return classes;
    }

    @Override
    protected void begin_scenario(IRubyObject scenario) throws Throwable {
        clearHooksAndStepDefinitions();
        objectFactory.createObjects();
        for (ClassAnalyzer analyzer : analyzers) {
            analyzer.populateStepDefinitionsAndHooks(objectFactory, this);
        }

    }

    @Override
    public void end_scenario() throws Throwable {
        objectFactory.disposeObjects();
    }

    private Class<?> loadClass(String classFile) throws ClassNotFoundException {
        String withoutExt = classFile.substring(0, classFile.length() - ".class".length());
        String[] pathElements = withoutExt.split("\\/");
        String className = null;
        for (int i = pathElements.length - 1; i >= 0; i--) {
            if (className == null) {
                className = pathElements[i];
            } else {
                className = pathElements[i] + "." + className;
            }
            try {
                return JRuby.getRuntime().getJRubyClassLoader().loadClass(className);
            } catch (ClassNotFoundException ignore) {
            }
        }
        throw new ClassNotFoundException("Couldn't determine class from file: " + classFile);
    }

    private static ObjectFactory createObjectFactory() throws Throwable {
        String objectFactoryClassName = System.getProperty("cuke4duke.objectFactory", "cuke4duke.internal.jvmclass.PicoFactory");
        Class<?> ofc = JRuby.getRuntime().getJRubyClassLoader().loadClass(objectFactoryClassName);
        Constructor<?> ctor = ofc.getConstructor();
        try {
            return (ObjectFactory) ctor.newInstance();
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

}
