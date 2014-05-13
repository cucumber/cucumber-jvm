package cucumber.runtime.java.spring;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestContextManager;
import cucumber.runtime.CucumberException;
import cucumber.runtime.java.ObjectFactory;

/**
 * Spring based implementation of ObjectFactory.
 * <p/>
 * <p>
 * <ul>
 * <li>It uses TestContextManager to create and prepare test instances. Configuration via: @ContextConfiguration
 * </li>
 * <li>It also uses a context which contains the step definitions and is reloaded for each
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

    private static ConfigurableApplicationContext applicationContext;
    private static ConfigurableListableBeanFactory beanFactory;

    private final Collection<Class<?>> stepClasses = new HashSet<Class<?>>();
    private CucumberTestContextManager testContextManager;

    private Class<?> stepClassWithSpringContext = null;

    public SpringFactory() {
    }

    static {
        applicationContext = new GenericXmlApplicationContext("cucumber/runtime/java/spring/cucumber-glue.xml");
        applicationContext.registerShutdownHook();
        beanFactory = applicationContext.getBeanFactory();
    }

    @Override
    public void addClass(final Class<?> stepClass) {
        if (!stepClasses.contains(stepClass)) {
            if (dependsOnSpringContext(stepClass)) {
                if (stepClassWithSpringContext == null) {
                    stepClassWithSpringContext = stepClass;
                } else {
                    checkAnnotationsEqual(stepClassWithSpringContext, stepClass);
                }
            }
            stepClasses.add(stepClass);

            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
            BeanDefinition beanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(stepClass)
                    .setScope(GlueCodeScope.NAME)
                    .getBeanDefinition();
            registry.registerBeanDefinition(stepClass.getName(), beanDefinition);
        }
    }

    private void checkAnnotationsEqual(Class<?> stepClassWithSpringContext, Class<?> stepClass) {
        Annotation[] annotations1 = stepClassWithSpringContext.getAnnotations();
        Annotation[] annotations2 = stepClass.getAnnotations();
        if (annotations1.length != annotations2.length) {
            throw new CucumberException("Annotations differs on glue classes found: " +
                    stepClassWithSpringContext.getName() + ", " +
                    stepClass.getName());
        }
        for (Annotation annotation : annotations1) {
            if (!isAnnotationInArray(annotation, annotations2)) {
                throw new CucumberException("Annotations differs on glue classes found: " +
                        stepClassWithSpringContext.getName() + ", " +
                        stepClass.getName());
            }
        }
    }

    private boolean isAnnotationInArray(Annotation annotation, Annotation[] annotations) {
        for (Annotation annotationFromArray: annotations) {
            if (annotation.equals(annotationFromArray)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        if (stepClassWithSpringContext == null) {
            throw new CucumberException("No glue class with spring annotation found");
        }
        GlueCodeContext.INSTANCE.start();
    }

    @Override
    public void stop() {
        notifyContextManagerAboutTestClassFinished();

        GlueCodeContext.INSTANCE.stop();
        beanFactory.destroySingletons();
    }

    private void notifyContextManagerAboutTestClassFinished() {
        if (testContextManager != null) {
            try {
                testContextManager.afterTestClass();
            } catch (Exception e) {
                throw new CucumberException(e.getMessage(), e);
            }
        }
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        if (!beanFactory.containsSingleton(type.getName())) {
            beanFactory.registerSingleton(type.getName(), getTestInstance(type));
        }

        return applicationContext.getBean(type);
    }

    private <T> T getTestInstance(final Class<T> type) {
        try {
            T instance = createTest(type);

            if (stepClassWithSpringContext != null) {
                if (testContextManager == null) {
                    testContextManager = new CucumberTestContextManager(stepClassWithSpringContext);
                    testContextManager.setParentOnApplicationContext(applicationContext);
                    testContextManager.prepareTestInstance(instance);
                    testContextManager.beforeTestClass();
                } else {
                    testContextManager.prepareTestInstance(instance);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new CucumberException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T createTest(Class<T> type) throws Exception {
        return (T) type.getConstructors()[0].newInstance();
    }

    private boolean dependsOnSpringContext(Class<?> type) {
        return type.isAnnotationPresent(ContextConfiguration.class)
            || type.isAnnotationPresent(ContextHierarchy.class);
    }
}

class CucumberTestContextManager extends TestContextManager {

    public CucumberTestContextManager(Class<?> testClass) {
        super(testClass);
    }

    @SuppressWarnings("resource")
    public void setParentOnApplicationContext(ApplicationContext parentContext) {
        ConfigurableApplicationContext context =
                (ConfigurableApplicationContext)getTestContext().getApplicationContext();
        while (context.getParent() != null && !context.getParent().equals(parentContext)) {
            context = (ConfigurableApplicationContext)context.getParent();
        }
        if (context.getParent() == null) {
            context.setParent(parentContext);
        }
    }
}
