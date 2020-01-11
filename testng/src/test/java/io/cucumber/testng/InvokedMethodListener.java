package io.cucumber.testng;

import java.util.*;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public final class InvokedMethodListener implements IInvokedMethodListener {

    private final Set<String> invokedConfigurationMethodNames = new HashSet<>();
    private final List<String> invokedTestMethodNames = new ArrayList<>();

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        String methodName = method.getTestMethod().getMethodName();
        if (method.isConfigurationMethod()) {
            invokedConfigurationMethodNames.add(methodName);
        } else if (method.isTestMethod()) {
            invokedTestMethodNames.add(methodName);
        }
    }

    public Set<String> getInvokedConfigurationMethodNames() {
        return Collections.unmodifiableSet(invokedConfigurationMethodNames);
    }

    public List<String> getInvokedTestMethodNames() {
        return Collections.unmodifiableList(invokedTestMethodNames);
    }
}
