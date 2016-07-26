package cucumber.metrics.regulator.module;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

import com.google.inject.Binder;
import com.google.inject.Module;

import cucumber.metrics.annotation.SpeedRegulator;
import cucumber.metrics.annotation.SpeedRegulators;
import cucumber.metrics.annotation.Timed;
import cucumber.metrics.regulator.interceptor.SpeedRegulatorInterceptor;
import cucumber.metrics.regulator.interceptor.TimedInterceptor;

public class SpeedRegulatorModule implements Module {

    @Override
    public void configure(Binder binder) {
        System.out.println("Cucumber Metrics SpeedRegulatorModule configure");
        SpeedRegulatorInterceptor speedRegulatorInterceptor = new SpeedRegulatorInterceptor();
        binder.bindInterceptor(any(), annotatedWith(SpeedRegulator.class), speedRegulatorInterceptor);
        binder.bindInterceptor(any(), annotatedWith(SpeedRegulators.class), speedRegulatorInterceptor);
        binder.bindInterceptor(any(), annotatedWith(Timed.class), new TimedInterceptor());
    }

}
