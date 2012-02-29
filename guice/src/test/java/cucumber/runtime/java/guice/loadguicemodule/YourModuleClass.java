package cucumber.runtime.java.guice.loadguicemodule;

import com.google.inject.AbstractModule;

public class YourModuleClass extends AbstractModule {

    @Override
    protected void configure() {
        bind(SharedBetweenSteps.class).toInstance(new SharedBetweenSteps());
    }
}