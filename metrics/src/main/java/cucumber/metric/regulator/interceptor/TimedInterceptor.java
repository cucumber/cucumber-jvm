package cucumber.metric.regulator.interceptor;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import cucumber.metric.annotation.Timed;
import cucumber.metric.core.impl.Meter;
import cucumber.metric.jmx.impl.Premier;

public class TimedInterceptor implements MethodInterceptor {

    private final ConcurrentMap<String, Meter> meters = new ConcurrentHashMap<String, Meter>();

    private MBeanServer mbs = null;

    Premier mbean = null;

    public TimedInterceptor() {
        this.mbean = new Premier();
        this.mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            this.mbs.registerMBean(this.mbean, new ObjectName("com.jmdoudoux.tests.jmx:type=PremierMBean"));
        } catch (InstanceAlreadyExistsException e) {
        } catch (MBeanRegistrationException e) {
        } catch (NotCompliantMBeanException e) {
        } catch (MalformedObjectNameException e) {
        }
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //
        Method m = invocation.getMethod();

        if (m.isAnnotationPresent(Timed.class)) {
            String methodName = "";
            Timed annotation = m.getAnnotation(Timed.class);
            if ("".equals(annotation.name())) {
                methodName = m.getName();
            } else {
                methodName = annotation.name();
            }
            System.out.println("Timed name:" + methodName);

            Meter meter = meters.containsKey(methodName) ? meters.get(methodName) : new Meter();
            meter.mark();
            meters.put(methodName, meter);
            System.out.println(meters.get(methodName).getCount());

            // JMX
            mbean.setValeur(mbean.getValeur() + 1);
        }
        //

        System.out.println("Cucumber Metrics TimedInterceptor invoke method " + invocation.getMethod() + " is called on " + invocation.getThis() + " with args " + invocation.getArguments());
        Object result = invocation.proceed();
        System.out.println("method " + invocation.getMethod() + " returns " + result);
        return result;
    }

}
