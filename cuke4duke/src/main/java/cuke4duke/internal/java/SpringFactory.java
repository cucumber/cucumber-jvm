package cuke4duke.internal.java;

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

    public void dispose() {
    }

    public Object getComponent(Class<?> type) {
        List beans = new ArrayList(appContext.getBeansOfType(type).values());
        if(beans.size() == 1) {
            return beans.get(0);
        } else {
            throw new RuntimeException("Found " + beans.size() + "Beans for class " + type + ". Expected exactly 1.");
        }
    }

    public void addClass(Class clazz) {
    }

    public void newWorld() {
        appContext.refresh();
    }
}
