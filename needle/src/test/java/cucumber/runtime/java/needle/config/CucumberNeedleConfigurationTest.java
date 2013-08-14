package cucumber.runtime.java.needle.config;

import static cucumber.runtime.java.needle.config.CucumberNeedleConfiguration.isInjectionProvider;
import static cucumber.runtime.java.needle.config.CucumberNeedleConfiguration.isInjectionProviderInstanceSupplier;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cucumber.api.needle.InjectionProviderInstancesSupplier;
import cucumber.runtime.java.needle.test.injectionprovider.SimpleNameGetterProvider;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;

public class CucumberNeedleConfigurationTest {

    public abstract static class A implements InjectionProviderInstancesSupplier {
    };

    @Test
    public void shouldReturnEmptyInstances() {
        final InjectionProvider<?>[] allInjectionProviders = new CucumberNeedleConfiguration("resource-bundles/empty")
                .getInjectionProviders();
        assertNotNull(allInjectionProviders);
        assertThat(allInjectionProviders.length, is(0));
    }

    @Test
    public void shouldEvaluateIfTypeIsInjectionProviderOrSupplier() throws Exception {
        assertTrue(isInjectionProvider(SimpleNameGetterProvider.class));
        assertFalse(isInjectionProviderInstanceSupplier(SimpleNameGetterProvider.class));
        assertFalse(isInjectionProvider(A.class));
        assertTrue(isInjectionProviderInstanceSupplier(A.class));
    }
}
