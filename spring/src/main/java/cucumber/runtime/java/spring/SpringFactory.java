package cucumber.runtime.java.spring;

import static java.util.Arrays.asList;
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
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestContextManager;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Spring based implementation of ObjectFactory.
 * <p>
 * Application beans are accessible from the step definitions using autowiring
 * (with annotations).
 * <p>
 * SpringFactory uses TestContextManager to manage the spring context. The step definitions are added to the
 * TestContextManagers context and the context is reloaded for each scenario.
 * <p>
 * The spring context can be configured by:
 * <ul>
 * <li>Annotating one step definition with: @{@link ContextConfiguration},  @{@link ContextHierarchy}
 * or @{@link BootstrapWith}. This step definition can also be annotated
 * with @{@link org.springframework.test.context.web.WebAppConfiguration}
 * or @{@link org.springframework.test.annotation.DirtiesContext} annotation.
 * <p>
 * If more that one step definition class has such an annotation, the annotations must be equal on
 * the different step definition. <b>Deprecation warning:</b> Annotating multiple step definitions is deprecated.
 * </li>
 * <li>If no step definition class with @ContextConfiguration or @ContextHierarchy
 * is found, it will try to load cucumber.xml from the classpath.</li>
 * </ul>
 * <p>
 * Notes:
 * <ul>
 * <li>
 * Step definitions should not be annotated with @{@link Component} or other annotations that mark it as eligible for
 * detection by classpath scanning. When a step definition class is annotated by @Component or an annotation that has
 * the @Component stereotype an exception will be thrown
 * </li>
 * <li>
 * If more that one step definition class is used to configure the spring context, the annotations must be equal
 * on the different step definition. Please note that doing so is deprecated.
 * </li>
 * </ul>
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
            if (hasComponentAnnotation(annotation)) {
                throw new CucumberException(String.format("" +
                        "Glue class %1$s was annotated with @%2$s; marking it as a candidate for auto-detection by " +
                        "Spring. Glue classes are detected and registered by Cucumber. Auto-detection of glue classes by " +
                        "spring may lead to duplicate bean definitions. Please remove the @%2$s annotation",
                    type.getName(),
                    annotation.annotationType().getSimpleName()));
            }
        }
    }

    private static boolean hasComponentAnnotation(Annotation annotation) {
        return hasAnnotation(annotation, Collections.<Class<? extends Annotation>>singleton(Component.class));
    }

    private static boolean hasAnnotation(Annotation annotation, Collection<Class<? extends Annotation>> desired) {
        Set<Class<? extends Annotation>> seen = new HashSet<Class<? extends Annotation>>();
        Stack<Class<? extends Annotation>> toCheck = new Stack<Class<? extends Annotation>>();
        toCheck.add(annotation.annotationType());

        while (!toCheck.isEmpty()) {
            Class<? extends Annotation> annotationType = toCheck.pop();
            if (desired.contains(annotationType)) {
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
        	Class enhanced = new GlueScopeConfigEnhancer().enhance(stepClassWithSpringContext);
            testContextManager = new CucumberTestContextManager(enhanced);
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

    private static boolean dependsOnSpringContext(Class<?> type) {
        for (Annotation annotation : type.getAnnotations()) {
            if(annotatedWithSupportedSpringRootTestAnnotations(annotation)){
                return true;
            }
        }
        return false;
    }

    private static boolean annotatedWithSupportedSpringRootTestAnnotations(Annotation type) {
        return hasAnnotation(type, asList(
            ContextConfiguration.class,
            ContextHierarchy.class,
            BootstrapWith.class));
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
