package cucumber.metrics.module;

import com.google.inject.Binder;

import cucumber.metrics.annotation.time.Time;
import cucumber.metrics.annotation.time.Times;
import cucumber.metrics.interceptor.TimeInterceptor;

public class TimeModule extends AbstractMetricsModule {

    public static final String TIME_ANNOTATION_ENABLE = "Time.annotation.enable";
    public static final String TIMES_ANNOTATION_ENABLE = "Times.annotation.enable";

    @Override
    public void configure(Binder binder) {
        System.out.println("Cucumber Metrics Time configure");

        TimeInterceptor timeInterceptor = new TimeInterceptor();
        setAnnotation2Interceptors(binder, TIME_ANNOTATION_ENABLE, Time.class, timeInterceptor);
        setAnnotation2Interceptors(binder, TIMES_ANNOTATION_ENABLE, Times.class, timeInterceptor);
    }

}
