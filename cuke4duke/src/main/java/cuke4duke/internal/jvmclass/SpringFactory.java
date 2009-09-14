package cuke4duke.internal.jvmclass;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class SpringFactory implements ObjectFactory {
    private final AbstractApplicationContext appContext;

    public SpringFactory() {
        String springXml = System.getProperty("cuke4duke.springXml", "cucumber.xml");
        appContext = new ClassPathXmlApplicationContext(springXml);
    }

    public void createObjects() {
        appContext.refresh();
    }

    public void disposeObjects() {
    }

    public void addClass(Class<?> clazz) {
    }

    @SuppressWarnings("unchecked")
	public Object getComponent(Class<?> type) {
        List beans = new ArrayList(appContext.getBeansOfType(type).values());
        if(beans.size() == 1) {
            return beans.get(0);
        } else {
            throw new RuntimeException("Found " + beans.size() + " Beans for class " + type + ". Expected exactly 1.");
        }
    }
}
