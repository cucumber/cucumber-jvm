package io.cucumber.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;

import java.lang.reflect.Method;

class CucumberTestContext implements TestContext {

    private final TestContext delegate;
    private final Class<?> testClass;

    private Object testInstance;
    private Method testMethod;
    private Throwable testException;

    CucumberTestContext(TestContext delegate, Class<?> testClass) {
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
