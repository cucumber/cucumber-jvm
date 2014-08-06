package cucumber.runtime.java.picocontainer.configuration;

import cucumber.runtime.java.picocontainer.configuration.PicoConfigurer;
import cucumber.runtime.java.picocontainer.configuration.PicoMapper;

public class CallCountingConfigurer implements PicoConfigurer {

    public static int timesRun = 0;

    @Override
    public void configure(PicoMapper picoMapper) {
        timesRun++;
    }

}