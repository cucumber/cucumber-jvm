package io.cucumber.needle;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import io.cucumber.needle.test.injectionprovider.SimpleNameGetterProvider;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

public class NeedleFactoryTest {

    @Test
    public void shouldSetUpInjectionProviders() {

        final InjectionProvider<?>[] injectionProviders = NeedleFactory
            .setUpInjectionProviders();

        assertNotNull(injectionProviders);
        assertThat(injectionProviders.length, is(1));
        assertThat(injectionProviders[0].getClass().getCanonicalName(),
            is(SimpleNameGetterProvider.class.getCanonicalName()));
    }

}
