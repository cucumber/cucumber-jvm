package io.cucumber.needle;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import io.cucumber.needle.test.injectionprovider.SimpleNameGetterProvider;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CucumberNeedleConfigurationTest {

    @Test
    public void shouldReturnEmptyInstances() {
        final InjectionProvider<?>[] allInjectionProviders = new CucumberNeedleConfiguration("resource-bundles/empty")
            .getInjectionProviders();
        assertNotNull(allInjectionProviders);
        assertThat(allInjectionProviders.length, is(0));
    }

    @Test
    public void shouldEvaluateIfTypeIsInjectionProviderOrSupplier() throws Exception {
        assertTrue(CucumberNeedleConfiguration.isInjectionProvider(SimpleNameGetterProvider.class));
        assertFalse(CucumberNeedleConfiguration.isInjectionProviderInstanceSupplier(SimpleNameGetterProvider.class));
        assertFalse(CucumberNeedleConfiguration.isInjectionProvider(A.class));
        assertTrue(CucumberNeedleConfiguration.isInjectionProviderInstanceSupplier(A.class));
    }

    public abstract static class A implements InjectionProviderInstancesSupplier {
    }
}
