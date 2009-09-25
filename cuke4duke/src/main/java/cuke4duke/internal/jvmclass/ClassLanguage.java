package cuke4duke.internal.jvmclass;

import cuke4duke.internal.language.Hook;
import cuke4duke.internal.language.ProgrammingLanguage;
import cuke4duke.internal.language.StepArgument;
import cuke4duke.internal.language.StepDefinition;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ClassLanguage extends ProgrammingLanguage {
    private final ObjectFactory objectFactory;
    private final List<ClassAnalyzer> analyzers;
    private final ClassLanguageMixin classLanguageMixin;
    private List<Class<?>> classes = new ArrayList<Class<?>>();
    private List<Hook> befores;
    private List<StepDefinition> stepDefinitions;
    private List<Hook> afters;

    public ClassLanguage(ClassLanguageMixin languageMixin, List<ClassAnalyzer> analyzers) throws Throwable {
        super(languageMixin);
        this.classLanguageMixin = languageMixin;
        this.analyzers = analyzers;
        String className = System.getProperty("cuke4duke.objectFactory");
        if(className == null) {
            throw new RuntimeException("Missing system property: cuke4duke.objectFactory");
        }
        Class<?> ofc = Thread.currentThread().getContextClassLoader().loadClass(className);
        Constructor<?> ctor = ofc.getConstructor();
        try {
            objectFactory = (ObjectFactory) ctor.newInstance();
        } catch(InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public void load_code_file(String classFile) throws Throwable {
        Class<?> clazz = loadClass(classFile);
        if(!Modifier.isAbstract(clazz.getModifiers())) {
            objectFactory.addClass(clazz);
        }
        classes.add(clazz);
    }

    @Override
    public void begin_scenario() throws Throwable {
        befores = new ArrayList<Hook>();
        stepDefinitions = new ArrayList<StepDefinition>();
        afters = new ArrayList<Hook>();

        objectFactory.createObjects();
        for(ClassAnalyzer analyzer : analyzers){
            for(Class<?> clazz : classes){
                analyzer.populateStepDefinitionsAndHooksFor(clazz, objectFactory, befores, stepDefinitions, afters);
            }
        }
        for(Hook before : befores){
            before.invoke("before", null);
        }

    }

    @Override
    public List<IRubyObject> step_match_list(String step_name, String formatted_step_name) {
        List<IRubyObject> matches = new ArrayList<IRubyObject>();
        for(StepDefinition stepDefinition : stepDefinitions){
            List<StepArgument> arguments = stepDefinition.arguments_from(step_name);
            if(arguments != null){
                matches.add(classLanguageMixin.create_step_match(stepDefinition, step_name, formatted_step_name, arguments));
            }
        }
        return matches;
    }

    @Override
    public void end_scenario() throws Throwable {
        for(Hook after : afters){
            after.invoke("after", null);
        }
        objectFactory.disposeObjects();
        //dispose stepdefinitions
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
