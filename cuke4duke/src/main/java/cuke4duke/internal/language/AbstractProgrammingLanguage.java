package cuke4duke.internal.language;

import cuke4duke.PyString;
import cuke4duke.internal.JRuby;
import cuke4duke.internal.java.MethodInvoker;
import cuke4duke.internal.jvmclass.CantTransform;
import cuke4duke.internal.jvmclass.DefaultJvmTransforms;
import org.jruby.RubyArray;
import org.jruby.runtime.builtin.IRubyObject;

import java.lang.reflect.Method;
import java.util.*;

public abstract class AbstractProgrammingLanguage implements ProgrammingLanguage {
    protected final LanguageMixin languageMixin;
    protected final MethodInvoker methodInvoker = new MethodInvoker();
    private final Map<Class<?>, Method> transformMethods = new HashMap<Class<?>, Method>();
    private List<StepDefinition> stepDefinitions;

    public AbstractProgrammingLanguage(LanguageMixin languageMixin) {
        this.languageMixin = languageMixin;
        for(Method method : DefaultJvmTransforms.class.getDeclaredMethods()) {
            transformMethods.put(method.getReturnType(), method);
        }
    }

    final public RubyArray step_matches(String step_name, String formatted_step_name) throws Throwable {
        return JRuby.newArray(step_match_list(step_name, formatted_step_name));
    }

    public abstract void load_code_file(String file) throws Throwable;

    public final List<IRubyObject> step_match_list(String step_name, String formatted_step_name) throws Throwable {
        List<IRubyObject> matches = new ArrayList<IRubyObject>();
        for (StepDefinition stepDefinition : stepDefinitions) {
            List<StepArgument> arguments = stepDefinition.arguments_from(step_name);
            if (arguments != null) {
                matches.add(languageMixin.create_step_match(stepDefinition, step_name, formatted_step_name, arguments));
            }
        }
        return matches;
    }

    protected void clearHooksAndStepDefinitions() {
        languageMixin.clear_hooks();
        stepDefinitions = new ArrayList<StepDefinition>();
    }

    public void addBeforeHook(Hook before) {
        languageMixin.add_hook("before", before);
    }

    public void addStepDefinition(StepDefinition stepDefinition) {
        stepDefinitions.add(stepDefinition);
    }

    // This method is only used by JUnit
    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }

    public void addAfterHook(Hook after) {
        languageMixin.add_hook("after", after);
    }

    protected abstract void begin_scenario(IRubyObject scenario) throws Throwable;

    public abstract void end_scenario() throws Throwable;

    public void availableStepDefinition(String regexp_source, String file_colon_line) {
        languageMixin.available_step_definition(regexp_source, file_colon_line);
    }

    public void invoked(String regexp_source, String file_colon_line) {
        languageMixin.invoked_step_definition(regexp_source, file_colon_line);
    }

    protected Object[] transform(Object[] args, Class<?>[] parameterTypes, Locale locale) throws Throwable {
        Object[] transformed = new Object[args.length];
        for (int i = 0; i < transformed.length; i++) {
            transformed[i] = transformOne(args[i], parameterTypes[i], locale);
        }
        return transformed;
    }

    public Object transformOne(Object arg, Class<?> parameterType, Locale locale) throws Throwable {
        if(PyString.class.isAssignableFrom(arg.getClass())) {
            arg = ((PyString)arg).to_s();
        }
        if(parameterType.isAssignableFrom(arg.getClass())) {
            return arg;
        }
        Object customTransform = customTransform(arg, parameterType, null);
        if(customTransform != null) {
            return customTransform;
        } else {
            return defaultTransform(arg, parameterType, locale);
        }
    }

    private Object defaultTransform(Object arg, Class<?> parameterType, Locale locale) throws Throwable {
        Method transformMethod = transformMethods.get(parameterType);
        if(transformMethod == null) {
            throw new CantTransform(arg, parameterType);
        }
        return methodInvoker.invoke(transformMethod, null, new Object[]{arg, locale});
    }

    protected abstract Object customTransform(Object arg, Class<?> parameterType, Locale locale) throws Throwable;
}
