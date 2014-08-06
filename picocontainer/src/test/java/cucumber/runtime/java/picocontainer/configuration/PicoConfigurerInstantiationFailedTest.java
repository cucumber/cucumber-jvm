package cucumber.runtime.java.picocontainer.configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PicoConfigurerInstantiationFailedTest {

    @Test
    public void shouldFormatADescriptiveMessage() {
        PicoConfigurerInstantiationFailed exception = new PicoConfigurerInstantiationFailed("bogus.class", null);
        assertThat(exception.getMessage(),
                is("Instantiation of 'bogus.class' failed"));
    }

    @Test
    public void shouldIncludeTheOriginalThrowable() throws Exception {
        Exception original = new RuntimeException("original");
        PicoConfigurerInstantiationFailed exception = new PicoConfigurerInstantiationFailed("bogus.class", original);
        assertThat(exception.getCause(), is((Throwable) original));
    }

}
