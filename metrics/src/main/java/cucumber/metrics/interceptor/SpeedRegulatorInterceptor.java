package cucumber.metrics.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import cucumber.metrics.annotation.regulator.SpeedRegulator;
import cucumber.metrics.annotation.regulator.SpeedRegulators;
import cucumber.metrics.core.impl.Meter;

public class SpeedRegulatorInterceptor implements MethodInterceptor {

    private static Logger logger = Logger.getLogger(TimeInterceptor.class.getName());

    private final ConcurrentMap<String, Meter> speedometers = new ConcurrentHashMap<String, Meter>();

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //
        Method m = invocation.getMethod();

        //
        if (m.isAnnotationPresent(SpeedRegulator.class)) {
            SpeedRegulator annotation = m.getAnnotation(SpeedRegulator.class);
            speedLimiter(annotation);
        }
        if (m.isAnnotationPresent(SpeedRegulators.class)) {
            SpeedRegulators annotations = m.getAnnotation(SpeedRegulators.class);
            for (int i = 0; i < annotations.value().length; i++) {
                SpeedRegulator annotation = annotations.value()[i];
                speedLimiter(annotation);
            }
        }

        //
        logger.fine("Cucumber Metrics SpeedRegulatorInterceptor invoke method " + invocation.getMethod() + " is called on " + invocation.getThis() + " with args " + invocation.getArguments());
        Object result = invocation.proceed();
        logger.fine("method " + invocation.getMethod() + " returns " + result);
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