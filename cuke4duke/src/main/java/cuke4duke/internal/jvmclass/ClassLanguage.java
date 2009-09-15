package cuke4duke.internal.jvmclass;

import cuke4duke.internal.language.Hook;
import cuke4duke.internal.language.ProgrammingLanguage;
import cuke4duke.internal.language.StepDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;

public class ClassLanguage extends ProgrammingLanguage {
    private final ObjectFactory objectFactory;
    private final List<ClassAnalyzer> analyzers;
    private final ClassLanguageMixin classLanguageMixin;

    public ClassLanguage(ClassLanguageMixin languageMixin, List<ClassAnalyzer> analyzers) throws Throwable {
        super(languageMixin);
        this.classLanguageMixin = languageMixin;
        this.analyzers = analyzers;
        String className = System.getProperty("cuke4duke.objectFactory");
        if(className == null) {
            throw new RuntimeException("Missing system property: cuke4duke.objectFactory");
        }
        Class ofc = Thread.currentThread().getContextClassLoader().loadClass(className);
        Constructor ctor = ofc.getConstructor();
        try {
            objectFactory = (ObjectFactory) ctor.newInstance();
        } catch(InvocationTargetException e) {
            throw e.getTargetException();
        }
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
        for(ClassAnalyzer analyzer : analyzers) {
            analyzer.registerHooksAndStepDefinitionsFor(clazz, this);
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

    public void addHook(String phase, Hook hook, ClassAnalyzer analyzer) {
        classLanguageMixin.activate(analyzer);
        classLanguageMixin.add_hook(phase, hook);
    }

    public Object getTarget(Class<?> type) {
        Object target = objectFactory.getComponent(type);
        if(target == null) {
            throw new NullPointerException("Couldn't find object for type " + type);
        }
        return target;
    }

    public void addStepDefinition(StepDefinition stepDefinition, ClassAnalyzer analyzer) {
        classLanguageMixin.activate(analyzer);
        addStepDefinition(stepDefinition);
    }
}
