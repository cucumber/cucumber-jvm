package cucumber.runtime.java.spring;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import cucumber.runtime.CucumberException;
import cucumber.runtime.java.ObjectFactory;

public class SpringFactory implements ObjectFactory {
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private StaticApplicationContext stepDefContext = null;
    private AbstractApplicationContext appContext = null;

    public SpringFactory() {
        stepDefContext = new StaticApplicationContext();
        stepDefContext.refresh();
        appContext = new ClassPathXmlApplicationContext(new String[] { "cucumber.xml" }, stepDefContext);

    }

    @Override
    public void addClass(final Class<?> clazz) {
        if (!classes.contains(clazz)) {
            stepDefContext.registerSingleton(clazz.getName(), clazz);
        }
        classes.add(clazz);
    }

    @Override
    public void createInstances() {
        appContext.refresh();
    }

    @Override
    public void disposeInstances() {}

    @Override
    public <T> T getInstance(final Class<T> type) {
        final Collection<T> beans = appContext.getBeansOfType(type).values();
        if (beans.size() == 1)
            return beans.iterator().next();
        else
            throw new CucumberException("Found " + beans.size() + " Beans for class " + type + ". Expected exactly 1.");
    }
}
