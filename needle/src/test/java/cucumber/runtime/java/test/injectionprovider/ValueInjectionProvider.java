package cucumber.runtime.java.test.injectionprovider;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import de.akquinet.jbosscc.needle.injection.InjectionTargetInformation;

/**
 * Returns a value provider returning the value given in constructor.
 * 
 * @author Simon Zambrovski
 * 
 */
public class ValueInjectionProvider implements InjectionProvider<ValueGetter> {

    private final String value;

    public ValueInjectionProvider(String value) {
        this.value = value;
    }

    @Override
    public boolean verify(InjectionTargetInformation injectionTargetInformation) {
        return injectionTargetInformation.getType().isAssignableFrom(ValueGetter.class);
    }

    @Override
    public ValueGetter getInjectedObject(Class<?> injectionPointType) {
        return new ValueGetter() {
            @Override
            public String getValue() {
                return value;
            }
        };
    }

    @Override
    public Object getKey(InjectionTargetInformation injectionTargetInformation) {
        return ValueGetter.class;
    }

}
