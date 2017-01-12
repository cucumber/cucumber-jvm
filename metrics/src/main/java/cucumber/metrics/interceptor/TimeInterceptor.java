package cucumber.metrics.interceptor;

import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import cucumber.metrics.annotation.time.Time;
import cucumber.metrics.annotation.time.TimeName;
import cucumber.metrics.annotation.time.TimeValue;
import cucumber.metrics.annotation.time.Times;
import cucumber.metrics.core.impl.Meter;
import cucumber.metrics.jmx.TimedJmxDynamicMBean;

public class TimeInterceptor implements MethodInterceptor {

    private static Logger logger = Logger.getLogger(TimeInterceptor.class.getName());

    private final ConcurrentMap<String, Meter> meters = new ConcurrentHashMap<String, Meter>();

    private MBeanServer mbs = null;

    TimedJmxDynamicMBean mbean = null;

    public TimeInterceptor() {
        this.mbean = new TimedJmxDynamicMBean();
        this.mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            this.mbs.registerMBean(this.mbean, new ObjectName("cucumber.metrics.jmx:type=TimedJmxDynamicMBean"));
        } catch (InstanceAlreadyExistsException e) {
            logger.warning("TimedInterceptor Exception - InstanceAlreadyExistsException" + e);
        } catch (MBeanRegistrationException e) {
            logger.warning("TimedInterceptor Exception - MBeanRegistrationException" + e);
        } catch (NotCompliantMBeanException e) {
            logger.warning("TimedInterceptor Exception - NotCompliantMBeanException" + e);
        } catch (MalformedObjectNameException e) {
            logger.warning("TimedInterceptor Exception - MalformedObjectNameException" + e);
        }
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //
        Method m = invocation.getMethod();
        Annotation[][] as = m.getParameterAnnotations();
        Object[] args = invocation.getArguments();

        //
        if (m.isAnnotationPresent(Time.class)) {
            Time timeAnnotation = m.getAnnotation(Time.class);
            timeProceed(m, as, args, timeAnnotation);
        }
        if (m.isAnnotationPresent(Times.class)) {
            Times annotations = m.getAnnotation(Times.class);
            for (int i = 0; i < annotations.value().length; i++) {
                Time timeAnnotation = annotations.value()[i];
                timeProceed(m, as, args, timeAnnotation);
            }
        }

        //
        logger.fine("Cucumber Metrics TimedInterceptor invoke method " + invocation.getMethod() + " is called on " + invocation.getThis() + " with args " + invocation.getArguments());
        Object result = invocation.proceed();
        logger.fine("method " + invocation.getMethod() + " returns " + result);
        return result;
    }

    private void timeProceed(Method m, Annotation[][] as, Object[] args, Time timeAnnotation) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        String timedName = getTimeName(m, as, args, timeAnnotation);
        int timedMark = getTimeMark(as, args, timeAnnotation);
        logger.fine("Timed name:" + timedName + "  Timed mark:" + timedMark);

        timed(timedName, timedMark);
        logger.fine("Timed of :" + timedName + " is " + meters.get(timedName).getCount());

        // JMX
        mbean.setAttribute(new Attribute(timedName, meters.get(timedName).getCount()));
    }

    private void timed(String timedName, int timedMark) {
        Meter meter = meters.containsKey(timedName) ? meters.get(timedName) : new Meter();
        meter.mark(timedMark);
        meters.put(timedName, meter);
    }

    private int getTimeMark(Annotation[][] as, Object[] args, Time timeAnnotation) {
        int timedValue = 1;
        if (timeAnnotation.mark() == 1) {
            for (int i = 0; i < as.length; i++) {
                Annotation[] annotations = as[i];
                for (Annotation annotation2 : annotations) {
                    if (annotation2 instanceof TimeValue && args[i] instanceof Integer) {
                        timedValue = (Integer) args[i];
                    }
                }
            }
        }
        return timedValue;
    }

    private String getTimeName(Method m, Annotation[][] as, Object[] args, Time timeAnnotation) {
        String timedName = "";
        if ("".equals(timeAnnotation.name())) {
            timedName = m.getName();
        } else {
            if (timeAnnotation.name().startsWith("{") && timeAnnotation.name().endsWith("}")) {
                for (int i = 0; i < as.length; i++) {
                    Annotation[] annotations = as[i];
                    for (Annotation annotation2 : annotations) {
                        if (annotation2 instanceof TimeName && timeAnnotation.name().substring(1, timeAnnotation.name().length() - 1).equals(((TimeName) annotation2).value())
                                && args[i] instanceof String) {
                            timedName = (String) args[i];
                        }
                    }
                }
            } else {
                timedName = timeAnnotation.name();
            }
        }
        return timedName;
    }

}
