package cucumber.internal;

import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;

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
                addStepDefinitions(stepObject);
			}
        }
    }
}
