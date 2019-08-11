package io.cucumber.needle.test.injectionprovider;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import de.akquinet.jbosscc.needle.injection.InjectionTargetInformation;

public class SimpleNameGetterProvider implements InjectionProvider<NameGetter> {

    public static final String FOO = "foo";

    @Override
    public boolean verify(final InjectionTargetInformation injectionTargetInformation) {
        return injectionTargetInformation.getType().isAssignableFrom(NameGetter.class);
    }

    @Override
    public NameGetter getInjectedObject(final Class<?> injectionPointType) {
        return () -> FOO;
    }

    @Override
    public Object getKey(final InjectionTargetInformation injectionTargetInformation) {
        return NameGetter.class;
    }

}
