package cucumber.runtime.java.spring;

import cucumber.runtime.CucumberException;
import cucumber.runtime.java.ObjectFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.ArrayList;
import java.util.Collection;

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

    private StaticApplicationContext stepContext;
    private final Collection<Class<?>> stepClasses = new ArrayList<Class<?>>();

    static {
        applicationContext = new ClassPathXmlApplicationContext(new String[]{"cucumber.xml"});
        applicationContext.registerShutdownHook();
    }

    @Override
    public void addClass(final Class<?> clazz) {
        stepClasses.add(clazz);
    }

    @Override
    public void createInstances() {
        createNewStepContext();
        populateStepContext();
    }

    private void createNewStepContext() {
        stepContext = new StaticApplicationContext(applicationContext);
        AutowiredAnnotationBeanPostProcessor autowirer = new AutowiredAnnotationBeanPostProcessor();
        autowirer.setBeanFactory(stepContext.getBeanFactory());
        stepContext.getBeanFactory().addBeanPostProcessor(autowirer);
        stepContext.getBeanFactory().addBeanPostProcessor(new CommonAnnotationBeanPostProcessor());
    }

    private void populateStepContext() {
        for (Class<?> stepClass : stepClasses) {
            stepContext.registerSingleton(stepClass.getName(), stepClass);
        }
        stepContext.refresh();
    }

    @Override
    public void disposeInstances() {
        stepContext.close();
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        try {
            return stepContext.getBean(type);
        } catch (NoSuchBeanDefinitionException exception) {
            throw new CucumberException(exception.getMessage(), exception);
        }
    }

}
