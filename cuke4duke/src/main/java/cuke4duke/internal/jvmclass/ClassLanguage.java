package cuke4duke.internal.jvmclass;

import cuke4duke.Order;
import cuke4duke.StepMother;
import cuke4duke.internal.language.AbstractProgrammingLanguage;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class ClassLanguage extends AbstractProgrammingLanguage {
    private final ObjectFactory objectFactory;
    private final List<ClassAnalyzer> analyzers;
    private List<Class<?>> classes = new ArrayList<Class<?>>();

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

    @Override
    protected void begin_scenario(IRubyObject scenario) throws Throwable {
        clearHooksAndStepDefinitions();
        objectFactory.createObjects();
        List<Method> orderedMethods = orderedMethods();
        for (ClassAnalyzer analyzer : analyzers) {
            for(Method method : orderedMethods) {
                analyzer.populateStepDefinitionsAndHooksFor(method, objectFactory, this);
            }
        }
    }

    private List<Method> orderedMethods() {
        List<Method> methods = new ArrayList<Method>();
        for(Class clazz :  classes) {
            methods.addAll(Arrays.asList(clazz.getMethods()));
        }
        Collections.sort(methods, new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                return order(m1) - order(m2);
            }

            private int order(Method m) {
                Order order = m.getAnnotation(Order.class);
                return (order == null) ? Integer.MAX_VALUE : order.value();
            }
        });
        return methods;
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
                return Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException ignore) {
            }
        }
        throw new ClassNotFoundException("Couldn't determine class from file: " + classFile);
    }

    private static ObjectFactory createObjectFactory() throws Throwable {
        String objectFactoryClassName = System.getProperty("cuke4duke.objectFactory", "cuke4duke.internal.jvmclass.PicoFactory");
        Class<?> ofc = Thread.currentThread().getContextClassLoader().loadClass(objectFactoryClassName);
        Constructor<?> ctor = ofc.getConstructor();
        try {
            return (ObjectFactory) ctor.newInstance();
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

}
