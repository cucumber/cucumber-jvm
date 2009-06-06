package cuke4duke.internal;

import org.jruby.RubyArray;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;

public class SpringStepMother extends StepMother {
    private AbstractApplicationContext appContext = null;

    // TODO: Find out why this gets called and not the RubyArray one....
    public void setConfigs(Object configs) {
        setConfigs((RubyArray) configs);
    }

    public void setConfigs(RubyArray configs) {
        setConfigs((String[]) configs.toArray(new String[configs.getLength()]));
    }

    public void setConfigs(String[] configs) {
        appContext = new ClassPathXmlApplicationContext(configs);
    }

    public void newWorld() {
        stepDefinitions.clear();
        appContext.refresh();

        for (Class<?> clazz : classes) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> beans = appContext.getBeansOfType(clazz);

            for (Object stepObject : beans.values()) {
                addStepDefinitions(stepObject);
            }
        }
    }
}
