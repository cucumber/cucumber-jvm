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
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.function.Supplier;

import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;
import static org.springframework.beans.factory.config.AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;

class TestContextAdaptor {

    private static final Object monitor = new Object();
    private final TestContextManager delegate;
    private final ConfigurableApplicationContext applicationContext;
    private final Deque<Runnable> stopInvocations = new ArrayDeque<>();
    private Object delegateTestInstance;

    static TestContextAdaptor create(
            Supplier<TestContextManager> testContextManagerSupplier,
            Collection<Class<?>> glueClasses
    ) {
        synchronized (monitor) {
            // While under construction, the TestContextManager delegate will
            // build a cached version of the application context configuration.
            // Since Spring Boot 3 and in combination with AOT building this
            // configuration is not idempotent (#2686).
            TestContextManager delegate = testContextManagerSupplier.get();

            TestContext testContext = delegate.getTestContext();
            ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) testContext
                    .getApplicationContext();

            // The TestContextManager delegate makes the application context
            // available to other threads. Registering the glue however modifies
            // the application context. To avoid concurrent modification issues
            // (#1823, #1153, #1148, #1106) we do this serially.
            registerGlueCodeScope(applicationContext);
            registerStepClassBeanDefinitions(applicationContext.getBeanFactory(), glueClasses);

            return new TestContextAdaptor(delegate);
        }
    }

    TestContextAdaptor(TestContextManager delegate) {
        this.delegate = delegate;
        this.applicationContext = (ConfigurableApplicationContext) delegate.getTestContext().getApplicationContext();
    }

    final void start() {
        stopInvocations.push(this::notifyTestContextManagerAboutAfterTestClass);
        notifyContextManagerAboutBeforeTestClass();
        stopInvocations.push(this::stopCucumberTestContext);
        startCucumberTestContext();
        stopInvocations.push(this::disposeTestInstance);
        createAndPrepareTestInstance();
        stopInvocations.push(this::notifyTestContextManagerAboutAfterTestMethod);
        notifyTestContextManagerAboutBeforeTestMethod();
        stopInvocations.push(this::notifyTestContextManagerAboutAfterTestExecution);
        notifyTestContextManagerAboutBeforeExecution();
    }

    private void notifyContextManagerAboutBeforeTestClass() {
        try {
            delegate.beforeTestClass();
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    private void startCucumberTestContext() {
        CucumberTestContext.getInstance().start();
    }

    private void createAndPrepareTestInstance() {
        // Unlike JUnit, Cucumber does not have a single test class.
        // Springs TestContext however assumes we do, and we are expected to
        // create an instance of it using the default constructor.
        //
        // Users of Cucumber would however like to inject their step
        // definition classes into other step definition classes. This requires
        // that the test instance exists in the application context as a bean.
        //
        // Normally when a bean is pulled from the application context with
        // getBean it is also autowired. This will however conflict with
        // Springs DependencyInjectionTestExecutionListener. So we create
        // a raw bean here.
        //
        // This probably free from side effects, but at some point in the
        // future we may have to accept that the only way forward is to
        // construct instances annotated with @CucumberContextConfiguration
        // using their default constructor and now allow them to be injected
        // into other step definition classes.
        try {
            Class<?> beanClass = delegate.getTestContext().getTestClass();

            ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
            // Note: By providing AUTOWIRE_CONSTRUCTOR the
            // AbstractAutowireCapableBeanFactory does not invoke
            // 'populateBean' and effectively creates a raw bean.
            Object bean = beanFactory.autowire(beanClass, AUTOWIRE_CONSTRUCTOR, false);

            // But it works out well for us. Because now the
            // DependencyInjectionTestExecutionListener will invoke
            // 'autowireBeanProperties' which will populate the bean.
            delegate.prepareTestInstance(bean);

            // Because the bean is created by a factory, it is not added to
            // the application context yet.
            CucumberTestContext scenarioScope = CucumberTestContext.getInstance();
            scenarioScope.put(beanClass.getName(), bean);

            this.delegateTestInstance = bean;
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    private void notifyTestContextManagerAboutBeforeTestMethod() {
        try {
            Method dummyMethod = getDummyMethod();
            delegate.beforeTestMethod(delegateTestInstance, dummyMethod);
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    private static void registerGlueCodeScope(ConfigurableApplicationContext context) {
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

    private void notifyTestContextManagerAboutBeforeExecution() {
        try {
            delegate.beforeTestExecution(delegateTestInstance, getDummyMethod());
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    private static void registerStepClassBeanDefinitions(
            ConfigurableListableBeanFactory beanFactory, Collection<Class<?>> glueClasses
    ) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        for (Class<?> glue : glueClasses) {
            registerStepClassBeanDefinition(registry, glue);
        }
    }

    private static void registerStepClassBeanDefinition(BeanDefinitionRegistry registry, Class<?> glueClass) {
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

    final void stop() {
        // Cucumber only supports 1 set of before/after semantics while JUnit
        // and Spring have 2 sets. So here we use a stack to ensure we don't
        // invoke only the matching after methods for each before methods.
        CucumberBackendException lastException = null;
        for (Runnable stopInvocation : stopInvocations) {
            try {
                stopInvocation.run();
            } catch (CucumberBackendException e) {
                if (lastException != null) {
                    e.addSuppressed(lastException);
                }
                lastException = e;
            }
        }
        if (lastException != null) {
            throw lastException;
        }
    }

    private void notifyTestContextManagerAboutAfterTestClass() {
        try {
            delegate.afterTestClass();
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    private void stopCucumberTestContext() {
        CucumberTestContext.getInstance().stop();
    }

    private void disposeTestInstance() {
        delegateTestInstance = null;
    }

    private void notifyTestContextManagerAboutAfterTestMethod() {
        try {
            Object delegateTestInstance = delegate.getTestContext().getTestInstance();
            // Cucumber tests can throw exceptions, but we can't currently
            // get at them. So we provide null intentionally.
            // Cucumber also doesn't a single test method, so we provide a
            // dummy instead.
            delegate.afterTestMethod(delegateTestInstance, getDummyMethod(), null);
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    private void notifyTestContextManagerAboutAfterTestExecution() {
        try {
            Object delegateTestInstance = delegate.getTestContext().getTestInstance();
            // Cucumber tests can throw exceptions, but we can't currently
            // get at them. So we provide null intentionally.
            // Cucumber also doesn't a single test method, so we provide a
            // dummy instead.
            delegate.afterTestExecution(delegateTestInstance, getDummyMethod(), null);
        } catch (Exception e) {
            throw new CucumberBackendException(e.getMessage(), e);
        }
    }

    final <T> T getInstance(Class<T> type) {
        return applicationContext.getBean(type);
    }

    private Method getDummyMethod() {
        try {
            return TestContextAdaptor.class.getMethod("cucumberDoesNotHaveASingleTestMethod");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void cucumberDoesNotHaveASingleTestMethod() {

    }
}
