package cucumber.runtime.java.guice.impl;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class PropertiesLoaderTest {

    @Test
    public void propertiesAreLoaded() throws Exception {
        assertThat(PropertiesLoader.loadGuiceProperties(), notNullValue());
    }
}
