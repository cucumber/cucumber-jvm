package cucumber.metrics.regulator.injector;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

import cucumber.api.guice.CucumberModules;
import cucumber.metrics.regulator.module.SpeedRegulatorModule;
import cucumber.runtime.java.guice.InjectorSource;

public class CucumberMetricsInjectorSource implements InjectorSource {

    @Override
    public Injector getInjector() {
        return Guice.createInjector(Stage.PRODUCTION, CucumberModules.SCENARIO, new SpeedRegulatorModule());
    }

}