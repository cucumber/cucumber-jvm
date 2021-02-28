package io.cucumber.testng;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InvokedMethodListener implements IInvokedMethodListener {

    private final List<String> invokedConfigurationMethodNames = new ArrayList<>();
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

    public List<String> getInvokedConfigurationMethodNames() {
        return Collections.unmodifiableList(invokedConfigurationMethodNames);
    }

    public List<String> getInvokedTestMethodNames() {
        return Collections.unmodifiableList(invokedTestMethodNames);
    }

}
