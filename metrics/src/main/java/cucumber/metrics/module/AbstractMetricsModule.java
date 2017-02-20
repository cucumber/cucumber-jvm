package cucumber.metrics.module;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

import java.lang.annotation.Annotation;
import java.util.logging.Logger;

import org.aopalliance.intercept.MethodInterceptor;

import com.google.inject.Binder;
import com.google.inject.Module;

import cucumber.metrics.interceptor.TimeInterceptor;
import cucumber.runtime.Env;

public abstract class AbstractMetricsModule implements Module {

    private static Logger logger = Logger.getLogger(TimeInterceptor.class.getName());

    void setAnnotation2Interceptors(Binder binder, String annotationEnable, final Class<? extends Annotation> annotationType, MethodInterceptor... interceptors) {
        String ae = Env.INSTANCE.get(annotationEnable);
        if ("false".equals(ae)) {
            logger.info(annotationEnable + " set to false.");
        } else if ("true".equals(ae)) {
            logger.info(annotationEnable + " set to true.");
            binder.bindInterceptor(any(), annotatedWith(annotationType), interceptors);
        } else {
            logger.info(annotationEnable + " not set or wrong set (set to false by default).");
        }
    }

}
