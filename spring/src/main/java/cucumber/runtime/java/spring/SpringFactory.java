package cucumber.runtime.java.spring;

import cucumber.runtime.CucumberException;
import cucumber.runtime.java.ObjectFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.Collection;

public class SpringFactory implements ObjectFactory {
    private final StaticApplicationContext staticContext = new StaticApplicationContext();
    private final AbstractApplicationContext classpathContext;
    public SpringFactory() {
        staticContext.refresh();
        classpathContext = new ClassPathXmlApplicationContext(new String[]{"cucumber.xml"}, staticContext);
    }

    @Override
    public void addClass(final Class<?> clazz) {
        staticContext.registerSingleton(clazz.getName(), clazz);
    }

    @Override
    public void createInstances() {
        classpathContext.refresh();
    }

    @Override
    public void disposeInstances() {
        classpathContext.close();
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        final Collection<T> beans = classpathContext.getBeansOfType(type).values();
        if (beans.size() == 1)
            return beans.iterator().next();
        else
            throw new CucumberException("Found " + beans.size() + " Beans for class " + type + ". Expected exactly 1.");
    }
}
