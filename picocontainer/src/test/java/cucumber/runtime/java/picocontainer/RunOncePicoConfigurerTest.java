package cucumber.runtime.java.picocontainer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import cucumber.runtime.java.picocontainer.configuration.PicoConfigurer;
import cucumber.runtime.java.picocontainer.configuration.PicoMapper;

public class RunOncePicoConfigurerTest {

    @Test
    public void onlyConfiguresPicoOnce() {
        PicoMapper picoMapper = mock(PicoMapper.class);
        PicoConfigurer delegateConfigurer = mock(PicoConfigurer.class);

        RunOncePicoConfigurer runOncePicoConfigurer = new RunOncePicoConfigurer(delegateConfigurer);
        runOncePicoConfigurer.configure(picoMapper);
        runOncePicoConfigurer.configure(picoMapper);

        verify(delegateConfigurer, times(1)).configure(picoMapper);
    }

}
