package io.cucumber.spring;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CucumberTestContextManager extends TestContextManager {

    private final Map<Class<?>, TestContext> stepContext = new HashMap<>();

    CucumberTestContextManager(Class<?> stepClassWithSpringContext, Collection<Class<?>> stepClasses) {
        super(stepClassWithSpringContext);
        for (final Class<?> stepClass : stepClasses) {
            final TestContext delegate = getTestContext();
            stepContext.put(stepClass, new CucumberTestContext(delegate, stepClass));
        }
    }

    private TestContext getTestContext(Class<?> key) {
        return stepContext.get(key);
    }

    private List<TestExecutionListener> getReversedTestExecutionListeners() {
        List<TestExecutionListener> listenersReversed = new ArrayList<>(getTestExecutionListeners());
        Collections.reverse(listenersReversed);
        return listenersReversed;
    }

    void prepareTestInstance(Map<Class<?>, Object> testInstances) throws Exception {
        for (Map.Entry<Class<?>, Object> instance : testInstances.entrySet()) {
            TestContext testContext = getTestContext(instance.getKey());
            testContext.updateState(instance.getValue(), null, null);
            for (TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
                testExecutionListener.prepareTestInstance(testContext);
            }
        }
    }

    void beforeTestMethod(Map<Class<?>, Object> testInstances, Method testMethod) throws Exception {
        for (Map.Entry<Class<?>, Object> instance : testInstances.entrySet()) {
            TestContext testContext = getTestContext(instance.getKey());
            testContext.updateState(instance.getValue(), testMethod, null);
            for (TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
                testExecutionListener.beforeTestMethod(testContext);
            }
        }
    }

    void beforeTestExecution(Map<Class<?>, Object> testInstances, Method testMethod) throws Exception {
        for (Map.Entry<Class<?>, Object> instance : testInstances.entrySet()) {
            TestContext testContext = getTestContext(instance.getKey());
            testContext.updateState(instance.getValue(), testMethod, null);
            for (TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
                testExecutionListener.beforeTestExecution(testContext);
            }
        }
    }

    void afterTestExecution(Map<Class<?>, Object> testInstances, Method testMethod, Throwable exception) throws Exception {
        for (Map.Entry<Class<?>, Object> instance : testInstances.entrySet()) {
            TestContext testContext = getTestContext(instance.getKey());
            testContext.updateState(instance.getValue(), testMethod, exception);
            for (TestExecutionListener testExecutionListener : getReversedTestExecutionListeners()) {
                testExecutionListener.afterTestExecution(testContext);
            }
        }
    }

    void afterTestMethod(Map<Class<?>, Object> testInstances, Method testMethod, Throwable exception) throws Exception {
        for (Map.Entry<Class<?>, Object> instance : testInstances.entrySet()) {
            TestContext testContext = getTestContext(instance.getKey());
            testContext.updateState(instance.getValue(), testMethod, exception);
            for (TestExecutionListener testExecutionListener : getReversedTestExecutionListeners()) {
                testExecutionListener.afterTestMethod(testContext);
            }
        }
    }

}
