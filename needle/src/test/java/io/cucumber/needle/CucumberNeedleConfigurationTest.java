package io.cucumber.needle;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import io.cucumber.needle.test.injectionprovider.SimpleNameGetterProvider;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CucumberNeedleConfigurationTest {

    public abstract static class A implements InjectionProviderInstancesSupplier {
    }

    @Test
    public void shouldReturnEmptyInstances() {
        final InjectionProvider<?>[] allInjectionProviders = new CucumberNeedleConfiguration("resource-bundles/empty")
            .getInjectionProviders();
        assertThat(allInjectionProviders, is(notNullValue()));
        assertThat(allInjectionProviders.length, is(0));
    }

    @Test
    public void shouldEvaluateIfTypeIsInjectionProviderOrSupplier() {
        assertAll("Checking Needle Configuration",
            () -> assertTrue(CucumberNeedleConfiguration.isInjectionProvider(SimpleNameGetterProvider.class)),
            () -> assertFalse(CucumberNeedleConfiguration.isInjectionProviderInstanceSupplier(SimpleNameGetterProvider.class)),
            () -> assertFalse(CucumberNeedleConfiguration.isInjectionProvider(A.class)),
            () -> assertTrue(CucumberNeedleConfiguration.isInjectionProviderInstanceSupplier(A.class))
        );
    }

}
