package cucumber.runtime.java.spring;

import cucumber.runtime.CucumberException;
import cucumber.runtime.java.ObjectFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SpringFactory implements ObjectFactory {
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private AbstractApplicationContext appContext;
    private StaticApplicationContext stepDefContext;

    public SpringFactory() {
        stepDefContext = new StaticApplicationContext();
        stepDefContext.refresh();
        appContext = new ClassPathXmlApplicationContext(new String[]{"context.xml"}, stepDefContext);
    }

    public void createInstances() {
    }

    public void disposeInstances() {
    }

    public void addClass(Class<?> clazz) {
        if (!classes.contains(clazz)) {
            stepDefContext.registerSingleton(clazz.getName(), clazz);
        }
        classes.add(clazz);
    }

    public <T> T getInstance(Class<T> type) {
        Collection<T> beans = appContext.getBeansOfType(type).values();
        if (beans.size() == 1) {
            return beans.iterator().next();
        } else {
            throw new CucumberException("Found " + beans.size() + " Beans for class " + type + ". Expected exactly 1.");
        }
    }
}
