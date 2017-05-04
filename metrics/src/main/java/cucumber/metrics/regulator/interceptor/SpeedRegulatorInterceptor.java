package cucumber.metrics.regulator.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import cucumber.metrics.annotation.SpeedRegulator;
import cucumber.metrics.annotation.SpeedRegulators;
import cucumber.metrics.core.impl.Meter;

public class SpeedRegulatorInterceptor implements MethodInterceptor {

    private final ConcurrentMap<String, Meter> speedometers = new ConcurrentHashMap<String, Meter>();

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //
        Method m = invocation.getMethod();

        //
        if (m.isAnnotationPresent(SpeedRegulator.class)) {
            SpeedRegulator annotation = m.getAnnotation(SpeedRegulator.class);
            System.out.println("A:" + annotation.application());
            System.out.println("A:" + annotation.cost());
            speedLimiter(annotation);
        }
        if (m.isAnnotationPresent(SpeedRegulators.class)) {
            SpeedRegulators annotations = m.getAnnotation(SpeedRegulators.class);
            for (int i = 0; i < annotations.value().length; i++) {
                SpeedRegulator annotation = annotations.value()[i];
                System.out.println("B" + i + ": " + annotation.application());
                System.out.println("B" + i + ": " + annotation.cost());
                speedLimiter(annotation);
            }
        }

        //
        System.out.println("Cucumber Metrics SpeedRegulatorInterceptor invoke method " + invocation.getMethod() + " is called on " + invocation.getThis() + " with args " + invocation.getArguments());
        Object result = invocation.proceed();
        System.out.println("method " + invocation.getMethod() + " returns " + result);
        return result;
    }

    private void speedLimiter(SpeedRegulator annotation) {
        if (annotation.cost() != -1) {
            Meter meter = speedometers.containsKey(annotation.application()) ? speedometers.get(annotation.application()) : new Meter(annotation.cost());
            meter.waitIfNecessaryAndUpdateNextAvailableTime();
            speedometers.put(annotation.application(), meter);
        }
    }

}