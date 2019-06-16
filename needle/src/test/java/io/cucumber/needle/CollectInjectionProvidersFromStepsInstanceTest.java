package io.cucumber.needle;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

import de.akquinet.jbosscc.needle.NeedleTestcase;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;

public class CollectInjectionProvidersFromStepsInstanceTest {

    private final CollectInjectionProvidersFromStepsInstance function = CollectInjectionProvidersFromStepsInstance.INSTANCE;

    @NeedleInjectionProvider
    private final InjectionProviderInstancesSupplier supplier = new InjectionProviderInstancesSupplier() {

        @Override
        public Set<InjectionProvider<?>> get() {
            final HashSet<InjectionProvider<?>> result = new HashSet<InjectionProvider<?>>();

            result.add(NamedInjectionProvider.forNamedValue("foo", "bar"));

            return result;
        }
    };

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

    @ObjectUnderTest
    private A a;

    @Test
    public void shouldAddInjectionProviders() throws Exception {
        final InjectionProvider<?>[] injectionProviders = function.apply(this);
        assertThat(injectionProviders.length, is(1));

        new MyNeedleTestcase(injectionProviders).initMyTestcase(this);

        assertThat(a.bar, is("bar"));
    }

}
