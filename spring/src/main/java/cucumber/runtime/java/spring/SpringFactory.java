package cucumber.runtime.java.spring;

import static org.springframework.test.context.FixBootstrapUtils.createBootstrapContext;
import static org.springframework.test.context.FixBootstrapUtils.resolveTestContextBootstrapper;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.CucumberException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestContextManager;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Spring based implementation of ObjectFactory.
 * <p/>
 * <p>
 * <ul>
 * <li>It uses TestContextManager to manage the spring context.
 * Configuration via: @{@link ContextConfiguration} or @{@link ContextHierarchy}
 * At least one step definition class needs to have a @ContextConfiguration
 * or @ContextHierarchy annotation. If more that one step definition class has such
 * an annotation, the annotations must be equal on the different step definition
 * classes. If no step definition class with @ContextConfiguration or @ContextHierarchy
 * is found, it will try to load cucumber.xml from the classpath.
 * </li>
 * <li>The step definitions class with @ContextConfiguration or @ContextHierarchy
 * annotation, may also have a @{@link org.springframework.test.context.web.WebAppConfiguration}
 * or @{@link org.springframework.test.annotation.DirtiesContext} annotation.
 * </li>
 * <li>The step definitions are added to the TestContextManagers context and
 * is reloaded for each scenario.</li>
 * <li>Step definitions  should not be annotated with @{@link Component} or
 * other annotations that mark it as eligible for detection by classpath scanning.</li>
 * <li>When a step definition class is annotated by @Component or an annotation that has the @Component stereotype an
 * exception will be thrown</li>
 * </li>
 * </ul>
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
    private boolean deprecationWarningIssued = false;

    public SpringFactory() {
    }

    @Override
    public boolean addClass(final Class<?> stepClass) {
        if (!stepClasses.contains(stepClass)) {
            checkNoComponentAnnotations(stepClass);
            if (dependsOnSpringContext(stepClass)) {
                if (stepClassWithSpringContext == null) {
                    stepClassWithSpringContext = stepClass;
                } else {
                    checkAnnotationsEqual(stepClassWithSpringContext, stepClass);
                }
            }
            stepClasses.add(stepClass);
        }
        return true;
    }

    private static void checkNoComponentAnnotations(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            if (hasComponentStereoType(annotation)) {
                throw new CucumberException(String.format("" +
                        "Glue class %1$s was annotated with @%2$s; marking it as a candidate for auto-detection by " +
                        "Spring. Glue classes are detected and registered by Cucumber. Auto-detection of glue classes by " +
                        "spring may lead to duplicate bean definitions. Please remove the @%2$s annotation",
                    type.getName(),
                    annotation.annotationType().getSimpleName()));
            }
        }
    }

    private static boolean hasComponentStereoType(Annotation annotation) {
        Set<Class<? extends Annotation>> seen = new HashSet<Class<? extends Annotation>>();
        Stack<Class<? extends Annotation>> toCheck = new Stack<Class<? extends Annotation>>();
        toCheck.add(annotation.annotationType());

        while (!toCheck.isEmpty()) {
            Class<? extends Annotation> annotationType = toCheck.pop();
            if (Component.class.equals(annotationType)) {
                return true;
            }

            seen.add(annotationType);
            for (Annotation annotationTypesAnnotations : annotationType.getAnnotations()) {
                if (!seen.contains(annotationTypesAnnotations.annotationType())) {
                    toCheck.add(annotationTypesAnnotations.annotationType());
                }
            }

        }
        return false;
    }


    private void checkAnnotationsEqual(Class<?> stepClassWithSpringContext, Class<?> stepClass) {
        if (!deprecationWarningIssued) {
            deprecationWarningIssued = true;
            System.err.println(String.format("" +
                    "WARNING: Having more than one glue class that configures " +
                    "the spring context is deprecated. Found both %1$s and %2$s " +
                    "that attempt to configure the spring context.",
                    stepClass, stepClassWithSpringContext));
        }
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
        if (stepClassWithSpringContext != null) {
            testContextManager = new CucumberTestContextManager(stepClassWithSpringContext);
        } else {
            if (beanFactory == null) {
                beanFactory = createFallbackContext();
            }
        }
        notifyContextManagerAboutTestClassStarted();
        if (beanFactory == null || isNewContextCreated()) {
            beanFactory = testContextManager.getBeanFactory();
            for (Class<?> stepClass : stepClasses) {
                registerStepClassBeanDefinition(beanFactory, stepClass);
            }
        }
        GlueCodeContext.getInstance().start();
    }

    @SuppressWarnings("resource")
    private ConfigurableListableBeanFactory createFallbackContext() {
        ConfigurableApplicationContext applicationContext;
        if (getClass().getClassLoader().getResource("cucumber.xml") != null) {
            applicationContext = new ClassPathXmlApplicationContext("cucumber.xml");
        } else {
            applicationContext = new GenericApplicationContext();
        }
        applicationContext.registerShutdownHook();
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        beanFactory.registerScope(GlueCodeScope.NAME, new GlueCodeScope());
        for (Class<?> stepClass : stepClasses) {
            registerStepClassBeanDefinition(beanFactory, stepClass);
        }
        return beanFactory;
    }

    private void notifyContextManagerAboutTestClassStarted() {
        if (testContextManager != null) {
            try {
                testContextManager.beforeTestClass();
            } catch (Exception e) {
                throw new CucumberException(e.getMessage(), e);
            }
        }
    }

    private boolean isNewContextCreated() {
        if (testContextManager == null) {
            return false;
        }
        return !beanFactory.equals(testContextManager.getBeanFactory());
    }

    private void registerStepClassBeanDefinition(ConfigurableListableBeanFactory beanFactory, Class<?> stepClass) {
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
        GlueCodeContext.getInstance().stop();
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
        try {
            return beanFactory.getBean(type);
        } catch (BeansException e) {
            throw new CucumberException(e.getMessage(), e);
        }
    }

    private boolean dependsOnSpringContext(Class<?> type) {
        boolean hasStandardAnnotations = annotatedWithSupportedSpringRootTestAnnotations(type);

        if(hasStandardAnnotations) {
            return true;
        }

        final Annotation[] annotations = type.getDeclaredAnnotations();
        return (annotations.length == 1) && annotatedWithSupportedSpringRootTestAnnotations(annotations[0].annotationType());
    }

    private boolean annotatedWithSupportedSpringRootTestAnnotations(Class<?> type) {
        return type.isAnnotationPresent(ContextConfiguration.class)
            || type.isAnnotationPresent(ContextHierarchy.class);
    }
}

class CucumberTestContextManager extends TestContextManager {

    CucumberTestContextManager(Class<?> testClass) {
        // Does the same as TestContextManager(Class<?>) but creates a
        // DefaultCacheAwareContextLoaderDelegate that uses a thread local contextCache.
        super(resolveTestContextBootstrapper(createBootstrapContext(testClass)));
        registerGlueCodeScope(getContext());
    }

    ConfigurableListableBeanFactory getBeanFactory() {
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
