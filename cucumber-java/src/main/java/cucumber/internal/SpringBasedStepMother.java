package cucumber.internal;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import cucumber.Given;
import cucumber.Then;
import cucumber.When;

public class SpringBasedStepMother extends StepMother {
	private ClassPathXmlApplicationContext appContext = null;
	private String config;
    
	
	public SpringBasedStepMother() {
		super();
	}
	public SpringBasedStepMother(String config) {
		this();
		this.config = config;
	}
	public void setConfig(String config) {
		this.config = config;
	}
	
	public void newWorld() {
		if (appContext == null) {
			appContext = new ClassPathXmlApplicationContext(new String[] {config});
		} else {
			appContext.refresh();
		}
    	
        for(Class<?> stepsClass : stepsClasses) {
        	@SuppressWarnings("unchecked")
            Map<Object, Object> beans = appContext.getBeansOfType(stepsClass);

        	for (Object stepObject : beans.values()) {
            	for (Method method : stepObject.getClass().getMethods()) {
                    String regexpString = null;
                    if (method.isAnnotationPresent(Given.class)) {
                        regexpString = method.getAnnotation(Given.class).value();
                    } else if (method.isAnnotationPresent(When.class)) {
                        regexpString = method.getAnnotation(When.class).value();
                    } else if (method.isAnnotationPresent(Then.class)) {
                        regexpString = method.getAnnotation(Then.class).value();
                    }
                    if(regexpString != null) {
                        stepDefinitions.add(new StepDefinition(regexpString, stepObject, method));
                    }
                }
			}
        }
    }
}
