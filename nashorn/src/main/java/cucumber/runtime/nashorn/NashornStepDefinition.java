package cucumber.runtime.nashorn;

import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;

public class NashornStepDefinition implements StepDefinition {
	private final ScriptEngine engine;
	private final Bindings engineScope;
    private final Object jsStepDefinition;
    private final Object regexp;
    private final Object bodyFunc;
    private final StackTraceElement location;
    private final Object argumentsFromFunc;

    public NashornStepDefinition(ScriptEngine engine, Bindings engineScope, Object jsStepDefinition, Object regexp, Object bodyFunc, StackTraceElement location, Object argumentsFromFunc) {
    	this.engine = engine;
    	this.engineScope = engineScope;
        this.jsStepDefinition = jsStepDefinition;
        this.regexp = regexp;
        this.bodyFunc = bodyFunc;
        this.location = location;
        this.argumentsFromFunc = argumentsFromFunc;
    }

    public List<Argument> matchedArguments(Step step) {
    	List<Argument> args = (List<Argument>) callOnScriptObjectMirror(argumentsFromFunc, this.engineScope, new Object[]{step.getName(), this});
		return args;
    }
    
    private Object callOnScriptObjectMirror(Object obj, Object thiz, Object... args) {
    	Method method;
		try {
			method = obj.getClass().getMethod("call",Object.class, Object[].class);
			return method.invoke(obj, thiz, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
    }

    public String getLocation(boolean detail) {
        return location.getFileName() + ":" + location.getLineNumber();
    }

    @Override
    public Integer getParameterCount() {
    	/*
    	 * gets the 'length' property of the function, which is arguments.length basically
    	 */
		
    	Method method;
		try {
			method = bodyFunc.getClass().getMethod("get",Object.class);
			Object returnValue = method.invoke(bodyFunc, "length");
			return (Integer) returnValue;	
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
    }

    @Override
    public ParameterInfo getParameterType(int n, Type argumentType) {
        return new ParameterInfo(argumentType, null, null, null);
    }

    public void execute(I18n i18n, Object[] args) throws Throwable {
//        try {
//            bodyFunc.call(cx, scope, scope, args);
//        } catch (JavaScriptException e) {
//            Object value = e.getValue();
//            if (value instanceof NativeJavaObject) {
//                NativeJavaObject njo = (NativeJavaObject) value;
//                Object unwrapped = njo.unwrap();
//                if (unwrapped instanceof Throwable) {
//                    throw (Throwable) unwrapped;
//                }
//            }
//            throw e.getCause() == null ? e : e.getCause();
//        }
    	callOnScriptObjectMirror(bodyFunc, this.engineScope, args);
    }

    public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        return location.getFileName().equals(stackTraceElement.getFileName());
    }

    @Override
    public String getPattern() {
    	/*
    	 * in javascript, /asdf/.source will get the string of the regex
    	 * you can't just call .toString()
    	 */
    	Method method;
		try {
			method = regexp.getClass().getMethod("get",Object.class);
			Object returnValue = method.invoke(regexp, "source");
			return (String) returnValue;	
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

    @Override
    public boolean isScenarioScoped() {
        return false;
    }
}
