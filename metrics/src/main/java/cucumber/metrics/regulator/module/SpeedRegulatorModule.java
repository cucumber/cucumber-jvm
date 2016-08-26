package cucumber.metrics.regulator.module;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

import java.lang.annotation.Annotation;

import org.aopalliance.intercept.MethodInterceptor;

import com.google.inject.Binder;
import com.google.inject.Module;

import cucumber.metrics.annotation.SpeedRegulator;
import cucumber.metrics.annotation.SpeedRegulators;
import cucumber.metrics.annotation.Timed;
import cucumber.metrics.regulator.interceptor.SpeedRegulatorInterceptor;
import cucumber.metrics.regulator.interceptor.TimedInterceptor;
import cucumber.runtime.Env;

public class SpeedRegulatorModule implements Module {

    public static final String SPEED_REGULATOR_ANNOTATION_ENABLE = "SpeedRegulator.annotation.enable";
    public static final String SPEED_REGULATORS_ANNOTATION_ENABLE = "SpeedRegulators.annotation.enable";
    public static final String TIMED_ANNOTATION_ENABLE = "Timed.annotation.enable";

    @Override
    public void configure(Binder binder) {
        System.out.println("Cucumber Metrics SpeedRegulatorModule configure");

        SpeedRegulatorInterceptor speedRegulatorInterceptor = new SpeedRegulatorInterceptor();
        setAnnotation2Interceptors(binder, SPEED_REGULATOR_ANNOTATION_ENABLE, SpeedRegulator.class, speedRegulatorInterceptor);
        setAnnotation2Interceptors(binder, SPEED_REGULATORS_ANNOTATION_ENABLE, SpeedRegulators.class, speedRegulatorInterceptor);
        setAnnotation2Interceptors(binder, TIMED_ANNOTATION_ENABLE, Timed.class, new TimedInterceptor());
    }

    private void setAnnotation2Interceptors(Binder binder, String annotationEnable, final Class<? extends Annotation> annotationType, MethodInterceptor... interceptors) {
        String ae = Env.INSTANCE.get(annotationEnable);
        if ("false".equals(ae)) {
            System.out.println(annotationEnable + " set to false.");
        } else if ("true".equals(ae)) {
            System.out.println(annotationEnable + " set to true.");
            binder.bindInterceptor(any(), annotatedWith(annotationType), interceptors);
        } else {
            System.out.println(annotationEnable + " not set or wrong set (set to true by default).");
            binder.bindInterceptor(any(), annotatedWith(annotationType), interceptors);
        }
    }

}
