package cucumber.metric.regulator.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class SpeedRegulatorInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Cucumber Metrics SpeedRegulatorInterceptor invoke method " + invocation.getMethod() + " is called on " + invocation.getThis() + " with args " + invocation.getArguments());
        Object result = invocation.proceed();
        System.out.println("method " + invocation.getMethod() + " returns " + result);
        return result;
    }

}