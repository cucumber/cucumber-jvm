package cucumber.metrics.module;

import com.google.inject.Binder;

import cucumber.metrics.annotation.regulator.SpeedRegulator;
import cucumber.metrics.annotation.regulator.SpeedRegulators;
import cucumber.metrics.interceptor.SpeedRegulatorInterceptor;

public class SpeedRegulatorModule extends AbstractMetricsModule {

    public static final String SPEED_REGULATOR_ANNOTATION_ENABLE = "SpeedRegulator.annotation.enable";
    public static final String SPEED_REGULATORS_ANNOTATION_ENABLE = "SpeedRegulators.annotation.enable";

    @Override
    public void configure(Binder binder) {
        System.out.println("Cucumber Metrics SpeedRegulator configure");

        SpeedRegulatorInterceptor speedRegulatorInterceptor = new SpeedRegulatorInterceptor();
        setAnnotation2Interceptors(binder, SPEED_REGULATOR_ANNOTATION_ENABLE, SpeedRegulator.class, speedRegulatorInterceptor);
        setAnnotation2Interceptors(binder, SPEED_REGULATORS_ANNOTATION_ENABLE, SpeedRegulators.class, speedRegulatorInterceptor);
    }

}
