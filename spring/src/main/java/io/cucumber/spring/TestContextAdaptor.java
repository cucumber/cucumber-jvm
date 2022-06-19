package io.cucumber.spring;

import io.cucumber.core.backend.CucumberBackendException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;

import java.lang.reflect.Method;
import java.util.Collection;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

class TestContextAdaptor {

    private static final Object monitor = new Object();

    private final TestContextManager delegate;
    private final ConfigurableApplicationContext applicationContext;
    private final Collection<Class<?>> glueClasses;

    TestContextAdaptor(
            TestContextManager delegate,
            Collection<Class<?>> glueClasses
    ) {
        TestContext testContext = delegate.getTestContext();
        ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) testContext
                .getApplicationContext();
        this.delegate = delegate;
        this.applicationContext = applicationContext;
        this.glueClasses = glueClasses;
    }

    public final void start() {
        // The TestContextManager delegate makes the application context
        // available to other threads. Registering the glue however modifies the
        // application context. To avoid concurrent modification issues (#1823,
        // #1153, #1148, #1106) we do this serially.
        synchronized (monitor) {
            registerGlueCodeScope(applicationContext);
            registerStepClassBeanDefinitions(applicationContext.getBeanFactory());
        }
        notifyContextManagerAboutBeforeTestClass();
        CucumberTestContext.getInstance().start();
        notifyTestContextManagerAboutBeforeTestMethod();
    }

    private void notifyTestContextManagerAboutBeforeTestMethod() {
        try {
            Class<?> delegateTestClass = delegate.getTestContext().getTestClass();
            Object delegateTestInstance = applicationContext.getBean(delegateTestClass);
            Method dummyMethod = TestContextAdaptor.class.getMethod("cucumberDoesNotHaveASingleTestMethod");
            delegate.beforeTestMethod(delegateTestInstance, dummyMethod);
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    final void registerGlueCodeScope(ConfigurableApplicationContext context) {
        while (context != null) {
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            // Scenario scope may have already been registered by another
            // thread.
            Scope registeredScope = beanFactory.getRegisteredScope(SCOPE_CUCUMBER_GLUE);
            if (registeredScope == null) {
                beanFactory.registerScope(SCOPE_CUCUMBER_GLUE, new CucumberScenarioScope());
            }
            context = (ConfigurableApplicationContext) context.getParent();
        }
    }

    private void notifyContextManagerAboutBeforeTestClass() {
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
        // Don't invoke after test method when before test class was not invoked
        // this is implicit in the existence of an active the test context
        // session. This is not ideal, but Cucumber only supports 1 set of
        // before/after semantics while JUnit and Spring have 2 sets.
        if (CucumberTestContext.getInstance().isActive()) {
            notifyTestContextManagerAboutAfterTestMethod();
            CucumberTestContext.getInstance().stop();
        }
        notifyTestContextManagerAboutAfterTestClass();
    }

    private void notifyTestContextManagerAboutAfterTestClass() {
        try {
            delegate.afterTestClass();
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    private void notifyTestContextManagerAboutAfterTestMethod() {
        try {
            Object delegateTestInstance = delegate.getTestContext().getTestInstance();
            Method dummyMethod = TestContextAdaptor.class.getMethod("cucumberDoesNotHaveASingleTestMethod");
            delegate.afterTestMethod(delegateTestInstance, dummyMethod, null);
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
