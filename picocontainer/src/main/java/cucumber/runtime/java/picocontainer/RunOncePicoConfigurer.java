package cucumber.runtime.java.picocontainer;

import cucumber.runtime.java.picocontainer.configuration.PicoConfigurer;
import cucumber.runtime.java.picocontainer.configuration.PicoMapper;

public class RunOncePicoConfigurer implements PicoConfigurer {

    private PicoConfigurer delegateConfigurer;
    private boolean invoked = false;

    public RunOncePicoConfigurer(PicoConfigurer delegateConfigurer) {
        this.delegateConfigurer = delegateConfigurer;
    }

    @Override
    public void configure(PicoMapper picoMapper) {
        if (!invoked) {
            delegateConfigurer.configure(picoMapper);
            invoked = true;
        }
    }

}
