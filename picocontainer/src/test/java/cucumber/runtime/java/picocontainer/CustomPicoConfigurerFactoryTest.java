package cucumber.runtime.java.picocontainer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import cucumber.runtime.java.picocontainer.configuration.GreeterImplementation;
import cucumber.runtime.java.picocontainer.configuration.GreeterInterface;
import cucumber.runtime.java.picocontainer.configuration.NoOpPicoConfigurer;
import cucumber.runtime.java.picocontainer.configuration.PicoConfigurer;
import cucumber.runtime.java.picocontainer.configuration.PicoMapper;

public class CustomPicoConfigurerFactoryTest {

    @Test
    public void shouldGiveUsANoOpConfigurerIfNoPropertyExists() throws Exception {
        Properties emptyProperties = new Properties();
        PicoConfigurer configurer = getConfigurerFromProperties(emptyProperties);
        assertThat(configurer, is(NoOpPicoConfigurer.class));
    }

    @Test
    public void shouldGiveUsARunOnceConfigurerWrappingThePicoConfigurerSpecifiedInTheProperties() throws Exception {
        Properties properties = new Properties();
        properties.put("picoConfigurer", "cucumber.runtime.java.picocontainer.configuration.YourPicoConfigurer");
        PicoConfigurer configurer = getConfigurerFromProperties(properties);

        assertThat(configurer, is(RunOncePicoConfigurer.class));
        assertThatYourPicoConfigurerIsDelegatedTo(configurer);
    }

    private PicoConfigurer getConfigurerFromProperties(Properties properties) throws IOException {
        PropertyLoader propertyLoader = mock(PropertyLoader.class);
        when(propertyLoader.getProperties()).thenReturn(properties);

        CustomPicoConfigurerFactory configurerFactory = new CustomPicoConfigurerFactory(propertyLoader);
        return configurerFactory.getConfigurer();
    }

    private void assertThatYourPicoConfigurerIsDelegatedTo(PicoConfigurer configurer) {
        PicoMapper picoMapper = mock(PicoMapper.class);
        configurer.configure(picoMapper);
        verify(picoMapper).addClass(GreeterInterface.class, GreeterImplementation.class);
    }

}