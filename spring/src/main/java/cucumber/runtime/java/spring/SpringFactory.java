package cucumber.runtime.java.spring;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
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

    private final Collection<Class<?>> stepClasses = new HashSet<Class<?>>();
    private final Map<Class<?>, TestContextManager> contextManagersByClass = new HashMap<Class<?>, TestContextManager>();

    public SpringFactory() {
    }

    static {
        applicationContext = new GenericXmlApplicationContext("cucumber/runtime/java/spring/cucumber-glue.xml");
        applicationContext.registerShutdownHook();
    }

    @Override
    public void addClass(final Class<?> stepClass) {
        if (!stepClasses.contains(stepClass)) {
            stepClasses.add(stepClass);

            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory();
            BeanDefinition beanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(stepClass)
                    .setScope(GlueCodeScope.NAME)
                    .getBeanDefinition();
            registry.registerBeanDefinition(stepClass.getName(), beanDefinition);
        }
    }

    @Override
    public void start() {
        GlueCodeContext.INSTANCE.start();
    }

    @Override
    public void stop() {
        notifyContextManagersAboutTestClassFinished();

        GlueCodeContext.INSTANCE.stop();
        applicationContext.getBeanFactory().destroySingletons();
    }

    private void notifyContextManagersAboutTestClassFinished() {
        Map<Class<?>, Exception> exceptionsThrown = new HashMap<Class<?>, Exception>();

        for (Map.Entry<Class<?>, TestContextManager> classTestContextManagerEntry : contextManagersByClass
                .entrySet()) {
            try {
                classTestContextManagerEntry.getValue().afterTestClass();
            } catch (Exception e) {
                exceptionsThrown.put(classTestContextManagerEntry.getKey(), e);


            }
        }
        contextManagersByClass.clear();

        if (exceptionsThrown.size() == 1) {
            //ony one exception, throw an exception with the correct cause
            Exception e = exceptionsThrown.values().iterator().next();
            throw new CucumberException(e.getMessage(), e);
        } else if (exceptionsThrown.size() > 1) {
            //multiple exceptions but we can only have one cause, put relevant info in the exception message
            //to not lose the interesting data
            throw new CucumberException(getMultipleExceptionMessage(exceptionsThrown));
        }
    }

    private String getMultipleExceptionMessage(Map<Class<?>, Exception> exceptionsThrow) {
        StringBuilder exceptionsThrown = new StringBuilder(1000);
        exceptionsThrown.append("Multiple exceptions occurred during processing of the TestExecutionListeners\n\n");

        for (Map.Entry<Class<?>, Exception> classExceptionEntry : exceptionsThrow.entrySet()) {

            exceptionsThrown.append("Exception during processing of TestExecutionListeners of ");
            exceptionsThrown.append(classExceptionEntry.getKey());
            exceptionsThrown.append('\n');
            exceptionsThrown.append(classExceptionEntry.getValue().toString());
            exceptionsThrown.append('\n');

            StringWriter stackTraceStringWriter = new StringWriter();
            PrintWriter stackTracePrintWriter = new PrintWriter(stackTraceStringWriter);
            classExceptionEntry.getValue().printStackTrace(stackTracePrintWriter);
            exceptionsThrown.append(stackTraceStringWriter.toString());
            exceptionsThrown.append('\n');

        }
        return exceptionsThrown.toString();
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        if (!applicationContext.getBeanFactory().containsSingleton(type.getName())) {
            applicationContext.getBeanFactory().registerSingleton(type.getName(), getTestInstance(type));
        }
        return applicationContext.getBean(type);
    }

    private <T> T getTestInstance(final Class<T> type) {
        try {
            T instance = createTest(type);

            if (dependsOnSpringContext(type)) {
                TestContextManager contextManager = new TestContextManager(type);
                contextManager.prepareTestInstance(instance);
                contextManager.beforeTestClass();

                contextManagersByClass.put(type, contextManager);
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
        return type.isAnnotationPresent(ContextConfiguration.class);
    }
}
