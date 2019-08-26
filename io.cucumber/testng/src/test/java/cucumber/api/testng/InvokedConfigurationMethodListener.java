package cucumber.api.testng;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public final class InvokedConfigurationMethodListener implements IInvokedMethodListener {

    private Set<String> invokedMethodNames = new HashSet<String>();
    
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isConfigurationMethod()) {
            invokedMethodNames.add(method.getTestMethod().getMethodName());
        }
    }

    public Set<String> getInvokedMethodNames() {
        return Collections.unmodifiableSet(invokedMethodNames);
    }
    
}
