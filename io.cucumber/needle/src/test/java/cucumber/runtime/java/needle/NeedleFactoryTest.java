package cucumber.runtime.java.needle;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import cucumber.runtime.java.needle.config.CucumberNeedleConfiguration;
import cucumber.runtime.java.needle.test.injectionprovider.SimpleNameGetterProvider;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;

public class NeedleFactoryTest {

    @Test
    public void shouldSetUpInjectionProviders() throws Exception {

        final InjectionProvider<?>[] injectionProviders = NeedleFactory
                .setUpInjectionProviders(CucumberNeedleConfiguration.RESOURCE_CUCUMBER_NEEDLE);

        assertNotNull(injectionProviders);
        assertThat(injectionProviders.length, is(1));
        assertThat(injectionProviders[0].getClass().getCanonicalName(),
                is(SimpleNameGetterProvider.class.getCanonicalName()));
    }

}
