package cuke4duke.internal.jvmclass;

import cuke4duke.internal.language.AbstractProgrammingLanguage;
import cuke4duke.StepMother;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ClassLanguage extends AbstractProgrammingLanguage {
    private final ObjectFactory objectFactory;
    private final List<ClassAnalyzer> analyzers;
    private List<Class<?>> classes = new ArrayList<Class<?>>();

    public ClassLanguage(ClassLanguageMixin languageMixin, StepMother stepMother, List<ClassAnalyzer> analyzers) throws Throwable {
        super(languageMixin);
        this.analyzers = analyzers;
        String className = System.getProperty("cuke4duke.objectFactory");
        if(className == null) {
            throw new RuntimeException("Missing system property: cuke4duke.objectFactory");
        }
        Class<?> ofc = Thread.currentThread().getContextClassLoader().loadClass(className);
        Constructor<?> ctor = ofc.getConstructor();
        try {
            objectFactory = (ObjectFactory) ctor.newInstance();
            objectFactory.addStepMother(stepMother);
        } catch(InvocationTargetException e) {
            throw e.getTargetException();
        }
        for(ClassAnalyzer analyzer : analyzers){
            for(Class<?> clazz : analyzer.allwaysLoad()){
                objectFactory.addClass(clazz);
            }
        }
    }

    public void load_code_file(String classFile) throws Throwable {
        Class<?> clazz = loadClass(classFile);
        if(!Modifier.isAbstract(clazz.getModifiers())) {
            objectFactory.addClass(clazz);
        }
        classes.add(clazz);
    }

    protected void prepareScenario() throws Throwable {
        clearHooksAndStepDefinitions();
        objectFactory.createObjects();
        for(ClassAnalyzer analyzer : analyzers){
            for(Class<?> clazz : classes){
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
}
