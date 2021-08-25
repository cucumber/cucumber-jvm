package io.cucumber.spring;

import io.cucumber.core.backend.CucumberBackendException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;

import java.lang.reflect.Method;
import java.util.Collection;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

abstract class TestContextAdaptor {

    private static final Object monitor = new Object();

    private final TestContextManager delegate;
    private final ConfigurableApplicationContext applicationContext;
    private final Collection<Class<?>> glueClasses;

    protected TestContextAdaptor(
            TestContextManager delegate,
            ConfigurableApplicationContext applicationContext,
            Collection<Class<?>> glueClasses
    ) {
        this.delegate = delegate;
        this.applicationContext = applicationContext;
        this.glueClasses = glueClasses;
    }

    static TestContextAdaptor createTestContextManagerAdaptor(
            TestContextManager delegate,
            Collection<Class<?>> glueClasses
    ) {
        TestContext testContext = delegate.getTestContext();
        ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) testContext
                .getApplicationContext();
        return new TestContextAdaptor(delegate, applicationContext, glueClasses) {

        };
    }

    public final void start() {
        // The TestContextManager delegate makes the application context
        // available to other threads. Registering the glue however modifies the
        // application context. To avoid concurrent modification issues (#1823,
        // #1153, #1148, #1106) we do this serially.
        synchronized (monitor) {
            registerGlueCodeScope(applicationContext);
            notifyContextManagerAboutTestClassStarted();
            registerStepClassBeanDefinitions(applicationContext.getBeanFactory());
        }
        notifyTestContextManagerAboutBeforeTestMethod();
    }

    private void notifyTestContextManagerAboutBeforeTestMethod() {
        try {
            CucumberTestContext.getInstance().start();
            Class<?> testClass = delegate.getTestContext().getTestClass();
            Object testContextInstance = applicationContext.getBean(testClass);
            Method dummyMethod = TestContextAdaptor.class.getMethod("cucumberDoesNotHaveASingleTestMethod");
            delegate.beforeTestMethod(testContextInstance, dummyMethod);
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    final void registerGlueCodeScope(ConfigurableApplicationContext context) {
        while (context != null) {
            context.getBeanFactory().registerScope(SCOPE_CUCUMBER_GLUE, new CucumberScenarioScope());
            context = (ConfigurableApplicationContext) context.getParent();
        }
    }

    private void notifyContextManagerAboutTestClassStarted() {
        try {
            delegate.beforeTestClass();
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    final void registerStepClassBeanDefinitions(ConfigurableListableBeanFactory beanFactory) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        for (Class<?> glue : glueClasses) {
            registerStepClassBeanDefinition(registry, glue);
        }
    }

    private void registerStepClassBeanDefinition(BeanDefinitionRegistry registry, Class<?> glueClass) {
        String beanName = glueClass.getName();
        // Step definition may have already been
        // registered as a bean by another thread.
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        registry.registerBeanDefinition(beanName, BeanDefinitionBuilder
                .genericBeanDefinition(glueClass)
                .setScope(SCOPE_CUCUMBER_GLUE)
                .getBeanDefinition());
    }

    public final void stop() {
        notifyTestContextManagerAboutAfterTestMethod();
        CucumberTestContext.getInstance().stop();
        try {
            delegate.afterTestClass();
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    private void notifyTestContextManagerAboutAfterTestMethod() {
        try {
            CucumberTestContext.getInstance().start();
            Class<?> testClass = delegate.getTestContext().getTestClass();
            Object testContextInstance = applicationContext.getBean(testClass);
            Method dummyMethod = TestContextAdaptor.class.getMethod("cucumberDoesNotHaveASingleTestMethod");
            delegate.afterTestMethod(testContextInstance, dummyMethod, null);
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    final <T> T getInstance(Class<T> type) {
        return applicationContext.getBean(type);
    }

    public void cucumberDoesNotHaveASingleTestMethod() {

    }
}
