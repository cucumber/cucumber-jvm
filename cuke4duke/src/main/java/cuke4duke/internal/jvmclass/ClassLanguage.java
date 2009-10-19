package cuke4duke.internal.jvmclass;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import cuke4duke.StepMother;
import cuke4duke.internal.java.DefaultJavaTransforms;
import cuke4duke.internal.language.AbstractProgrammingLanguage;

public class ClassLanguage extends AbstractProgrammingLanguage {
    private final ObjectFactory objectFactory;
    private final List<ClassAnalyzer> analyzers;
    private List<Class<?>> classes = new ArrayList<Class<?>>();

    public ClassLanguage(ClassLanguageMixin languageMixin, StepMother stepMother, List<ClassAnalyzer> analyzers) throws Throwable {
        super(languageMixin);
        this.analyzers = analyzers;
        objectFactory = createObjectFactory();
        objectFactory.addStepMother(stepMother);
        objectFactory.addClass(DefaultJavaTransforms.class);
        for (ClassAnalyzer analyzer : analyzers) {
            for (Class<?> clazz : analyzer.alwaysLoad()) {
                objectFactory.addClass(clazz);
            }
        }
    }

    public void load_code_file(String classFile) throws Throwable {
        Class<?> clazz = loadClass(classFile);
        if (!Modifier.isAbstract(clazz.getModifiers())) {
            objectFactory.addClass(clazz);
        }
        classes.add(clazz);
    }

    protected void prepareScenario() throws Throwable {
        clearHooksAndStepDefinitions();
        objectFactory.createObjects();
        for (ClassAnalyzer analyzer : analyzers) {
            analyzer.addDefaultTransforms(this, objectFactory);
            for (Class<?> clazz : classes) {
                analyzer.populateStepDefinitionsAndHooksFor(clazz, objectFactory, this);
            }
        }
    }

    @Override
    public void cleanupScenario() throws Throwable {
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
                return Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException ignore) {
            }
        }
        throw new ClassNotFoundException("Couldn't determine class from file: " + classFile);
    }

    private ObjectFactory createObjectFactory() throws Throwable {
        String className = System.getProperty("cuke4duke.objectFactory", "cuke4duke.internal.jvmclass.PicoFactory");
        if (className == null) {
            throw new RuntimeException("Missing system property: cuke4duke.objectFactory");
        }
        Class<?> ofc = Thread.currentThread().getContextClassLoader().loadClass(className);
        Constructor<?> ctor = ofc.getConstructor();
        try {
            return (ObjectFactory) ctor.newInstance();
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

}
