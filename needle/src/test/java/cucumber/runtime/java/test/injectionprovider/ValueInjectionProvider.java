package cucumber.runtime.java.test.injectionprovider;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import de.akquinet.jbosscc.needle.injection.InjectionTargetInformation;

/**
 * Returns a value provider returning the value given in constructor.
 */
public class ValueInjectionProvider implements InjectionProvider<ValueGetter> {

    private final String value;

    public ValueInjectionProvider(final String value) {
        this.value = value;
    }

    @Override
    public boolean verify(final InjectionTargetInformation injectionTargetInformation) {
        return injectionTargetInformation.getType().isAssignableFrom(ValueGetter.class);
    }

    @Override
    public ValueGetter getInjectedObject(final Class<?> injectionPointType) {
        return new ValueGetter() {
            @Override
            public String getValue() {
                return value;
            }
        };
    }

    @Override
    public Object getKey(final InjectionTargetInformation injectionTargetInformation) {
        return ValueGetter.class;
    }

}
