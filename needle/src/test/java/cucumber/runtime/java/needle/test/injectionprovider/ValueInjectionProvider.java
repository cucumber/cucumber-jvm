package cucumber.runtime.java.needle.test.injectionprovider;

import cucumber.runtime.java.needle.injection.DefaultInstanceInjectionProvider;

/**
 * Returns a value provider returning the value given in constructor.
 */
public class ValueInjectionProvider extends DefaultInstanceInjectionProvider<ValueGetter> {

  public ValueInjectionProvider(final String value) {
    super(new ValueGetter() {
      @Override
      public String getValue() {
        return value;
      }
    });
  }

}
