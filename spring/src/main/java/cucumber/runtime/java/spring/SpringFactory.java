package cucumber.runtime.java.spring;

import cucumber.runtime.CucumberException;
import cucumber.runtime.java.ObjectFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestContextManager;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;

/**
 * Spring based implementation of ObjectFactory.
 * <p/>
 * <p>
 * <ul>
 * <li>It uses TestContextManager to create and prepare test instances.
 * Configuration via: @ContextConfiguration of @ContextHierarcy
 * At least on step definition class needs to have a @ContextConfiguration or
 * @ContextHierarchy annotation. If more that one step definition class has such
 * an annotation, the annotations must be equal on the different step definition
 * classes.</li>
 * <li>The step definitions class with @ContextConfiguration or @ContextHierarchy
 * annotation, may also have a @WebAppConfiguration or @DirtiesContext annotation.
 * </li>
 * <li>The step definitions added to the TestContextManagers context and
 * is reloaded for each scenario.</li>
 * </ul>
 * </p>
 * <p/>
 * <p>
 * Application beans are accessible from the step definitions using autowiring
 * (with annotations).
 * </p>
 */
public class SpringFactory implements ObjectFactory {

    private ConfigurableListableBeanFactory beanFactory;
    private CucumberTestContextManager testContextManager;

    private final Collection<Class<?>> stepClasses = new HashSet<Class<?>>();
    private Class<?> stepClassWithSpringContext = null;

    public SpringFactory() {
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
            throw new CucumberException("No glue class with spring annotation found. " +
                    "One glue class with @ContextConfiguration or " +
                    "@ContextHierarcy annotation is needed.");
        }
        testContextManager = new CucumberTestContextManager(stepClassWithSpringContext);
        notifyContextManagerAboutTestClassStarted();
        if (isFirstScenario() || isNewContextCreated()) {
            beanFactory = testContextManager.getBeanFactory();
            for (Class<?> stepClass : stepClasses) {
                registerStepClassBeanDefinition(stepClass);
            }
        }
        GlueCodeContext.INSTANCE.start();
    }

    private void notifyContextManagerAboutTestClassStarted() {
        try {
            testContextManager.beforeTestClass();
        } catch (Exception e) {
            throw new CucumberException(e.getMessage(), e);
        }
    }

    private boolean isFirstScenario() {
        return beanFactory == null;
    }

    private boolean isNewContextCreated() {
        return !beanFactory.equals(testContextManager.getBeanFactory());
    }

    private void registerStepClassBeanDefinition(Class<?> stepClass) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        BeanDefinition beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(stepClass)
                .setScope(GlueCodeScope.NAME)
                .getBeanDefinition();
        registry.registerBeanDefinition(stepClass.getName(), beanDefinition);
    }

    @Override
    public void stop() {
        notifyContextManagerAboutTestClassFinished();
        GlueCodeContext.INSTANCE.stop();
    }

    private void notifyContextManagerAboutTestClassFinished() {
        try {
            testContextManager.afterTestClass();
        } catch (Exception e) {
            throw new CucumberException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        try {
            return beanFactory.getBean(type);
        } catch (BeansException e) {
            throw new CucumberException(e.getMessage(), e);
        }
    }

    private boolean dependsOnSpringContext(Class<?> type) {
        return type.isAnnotationPresent(ContextConfiguration.class)
            || type.isAnnotationPresent(ContextHierarchy.class);
    }
}

class CucumberTestContextManager extends TestContextManager {

    public CucumberTestContextManager(Class<?> testClass) {
        super(testClass);
        registerGlueCodeScope(getContext());
    }

    public ConfigurableListableBeanFactory getBeanFactory() {
        return getContext().getBeanFactory();
    }

    private ConfigurableApplicationContext getContext() {
        return (ConfigurableApplicationContext)getTestContext().getApplicationContext();
    }

    private void registerGlueCodeScope(ConfigurableApplicationContext context) {
        do {
            context.getBeanFactory().registerScope(GlueCodeScope.NAME, new GlueCodeScope());
            context = (ConfigurableApplicationContext)context.getParent();
        } while (context != null);
    }
}
