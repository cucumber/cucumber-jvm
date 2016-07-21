package cucumber.metric.regulator.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class TimedInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Cucumber Metrics TimedInterceptor invoke method " + invocation.getMethod() + " is called on " + invocation.getThis() + " with args " + invocation.getArguments());
        Object result = invocation.proceed();
        System.out.println("method " + invocation.getMethod() + " returns " + result);
        return result;
    }

}
