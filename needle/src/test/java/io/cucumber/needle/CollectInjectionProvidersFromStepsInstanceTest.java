package io.cucumber.needle;

import de.akquinet.jbosscc.needle.NeedleTestcase;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CollectInjectionProvidersFromStepsInstanceTest {

    private final CollectInjectionProvidersFromStepsInstance function = CollectInjectionProvidersFromStepsInstance.INSTANCE;

    @NeedleInjectionProvider
    private final InjectionProviderInstancesSupplier supplier = () -> {
        final HashSet<InjectionProvider<?>> result = new HashSet<>();

        result.add(NamedInjectionProvider.forNamedValue("foo", "bar"));

        return result;
    };
    @ObjectUnderTest
    private A a;

    @Test
    void shouldAddInjectionProviders() throws Exception {
        final InjectionProvider<?>[] injectionProviders = function.apply(this);
        assertThat(injectionProviders.length, is(1));

        new MyNeedleTestcase(injectionProviders).initMyTestcase(this);

        assertThat(a.bar, is("bar"));
    }

    private static class MyNeedleTestcase extends NeedleTestcase {

        public MyNeedleTestcase(final InjectionProvider<?>... injectionProvider) {
            super(injectionProvider);
        }

        protected void initMyTestcase(final Object test) throws Exception {
            initTestcase(test);
        }

    }

    public static class A {

        @Inject
        @Named("foo")
        private String bar;

    }

}
