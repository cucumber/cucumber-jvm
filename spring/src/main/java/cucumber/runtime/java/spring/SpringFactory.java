package cucumber.runtime.java.spring;

import cucumber.runtime.CucumberException;
import cucumber.runtime.java.ObjectFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Collection;
import java.util.HashSet;

/**
 * Spring based implementation of ObjectFactory.
 * <p/>
 * <p>
 * It uses two Spring contexts:
 * <ul>
 * <li>one which represents the application under test. This is configured by
 * cucumber.xml (in the class path) and is never reloaded.</li>
 * <li>one which contains the step definitions and is reloaded for each
 * scenario.</li>
 * </ul>
 * </p>
 * <p/>
 * <p>
 * Application beans are accessible from the step definitions using autowiring
 * (with annotations).
 * </p>
 */
public class SpringFactory implements ObjectFactory {

    private static AbstractApplicationContext applicationContext;

    private final Collection<Class<?>> stepClasses = new HashSet<Class<?>>();

    public SpringFactory() {
    }

    static {
        applicationContext = new ClassPathXmlApplicationContext(
                "cucumber/runtime/java/spring/cucumber-glue.xml",
                "cucumber.xml");
        applicationContext.registerShutdownHook();
    }

    @Override
    public void addClass(final Class<?> stepClass) {
        if (!stepClasses.contains(stepClass)) {
            stepClasses.add(stepClass);

            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
            registry.registerBeanDefinition(stepClass.getName(),
                    BeanDefinitionBuilder.genericBeanDefinition(stepClass)
                            .setScope(GlueCodeScope.NAME)
                            .getBeanDefinition());

        }
    }

    @Override
    public void createInstances() {
        GlueCodeContext.INSTANCE.start();
    }

    @Override
    public void disposeInstances() {
        GlueCodeContext.INSTANCE.stop();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getInstance(final Class<T> type) {
        try {
            return applicationContext.getBean(type);
        } catch (NoSuchBeanDefinitionException exception) {
            throw new CucumberException(exception.getMessage(), exception);
        }
    }

}
