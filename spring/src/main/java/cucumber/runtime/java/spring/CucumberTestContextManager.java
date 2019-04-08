package cucumber.runtime.java.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListener;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class CucumberTestContextManager extends TestContextManager {

    private final Map<Class<?>, TestContext> stepContext = new HashMap<>();

    public CucumberTestContextManager(Class<?> stepClassWithSpringContext, Collection<Class<?>> stepClasses) {
        super(stepClassWithSpringContext);
        for (final Class<?> stepClass : stepClasses) {
            final TestContext delegate = getTestContext();
            stepContext.put(stepClass, new CucumberTestContext(delegate, stepClass));
        }
    }

    private TestContext getTestContext(Class<?> key) {
        return stepContext.get(key);
    }

    public void prepareTestInstance(Map<Class<?>, Object> testInstance) throws Exception {
        for (Map.Entry<Class<?>, Object> instance : testInstance.entrySet()) {
            TestContext testContext = getTestContext(instance.getKey());
            testContext.updateState(instance.getValue(), null, null);
            for (TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
                testExecutionListener.prepareTestInstance(testContext);
            }
        }
    }

    public void beforeTestMethod(Map<Class<?>, Object> testInstance, Method testMethod) throws Exception {
        for (Map.Entry<Class<?>, Object> instance : testInstance.entrySet()) {
            TestContext testContext = getTestContext(instance.getKey());
            testContext.updateState(instance.getValue(), testMethod, null);
            for (TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
                testExecutionListener.beforeTestMethod(testContext);
            }
        }
    }

    public void beforeTestExecution(Map<Class<?>, Object> testInstance, Method testMethod) throws Exception {
        for (Map.Entry<Class<?>, Object> instance : testInstance.entrySet()) {
            TestContext testContext = getTestContext(instance.getKey());
            testContext.updateState(instance.getValue(), testMethod, null);
            for (TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
                testExecutionListener.beforeTestExecution(testContext);
            }
        }
    }

    public void afterTestExecution(Map<Class<?>, Object> testInstance, Method testMethod, Throwable exception) throws Exception {
        for (Map.Entry<Class<?>, Object> instance : testInstance.entrySet()) {
            TestContext testContext = getTestContext(instance.getKey());
            testContext.updateState(instance.getValue(), testMethod, exception);
            for (TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
                testExecutionListener.afterTestExecution(testContext);
            }
        }
    }

    public void afterTestMethod(Map<Class<?>, Object> testInstance, Method testMethod, Throwable exception) throws Exception {
        for (Map.Entry<Class<?>, Object> instance : testInstance.entrySet()) {
            TestContext testContext = getTestContext(instance.getKey());
            testContext.updateState(instance.getValue(), testMethod, exception);
            for (TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
                testExecutionListener.afterTestMethod(testContext);
            }
        }
    }

    private static class CucumberTestContext implements TestContext {

        private final TestContext delegate;
        private final Class<?> testClass;

        @Nullable
        private volatile Object testInstance;

        @Nullable
        private volatile Method testMethod;

        @Nullable
        private volatile Throwable testException;

        public CucumberTestContext(TestContext delegate, Class<?> testClass) {
            this.delegate = delegate;
            this.testClass = testClass;
        }


        @Override
        public ApplicationContext getApplicationContext() {
            return delegate.getApplicationContext();
        }

        @Override
        public Class<?> getTestClass() {
            return testClass;
        }

        @Override
        public Object getTestInstance() {
            return testInstance;
        }

        @Override
        public Method getTestMethod() {
            return testMethod;
        }

        @Override
        public Throwable getTestException() {
            return testException;
        }

        @Override
        public void markApplicationContextDirty(DirtiesContext.HierarchyMode hierarchyMode) {
            delegate.markApplicationContextDirty(hierarchyMode);
        }

        @Override
        public void updateState(Object testInstance, Method testMethod, Throwable testException) {
            this.testInstance = testInstance;
            this.testMethod = testMethod;
            this.testException = testException;
        }

        @Override
        public void setAttribute(String s, Object o) {
            delegate.setAttribute(s, o);
        }

        @Override
        public Object getAttribute(String s) {
            return delegate.getAttribute(s);
        }

        @Override
        public Object removeAttribute(String s) {
            return delegate.removeAttribute(s);
        }

        @Override
        public boolean hasAttribute(String s) {
            return delegate.hasAttribute(s);
        }

        @Override
        public String[] attributeNames() {
            return delegate.attributeNames();
        }
    }
}
