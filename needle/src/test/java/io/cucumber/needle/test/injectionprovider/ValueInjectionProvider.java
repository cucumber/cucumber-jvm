package io.cucumber.needle.test.injectionprovider;

import io.cucumber.needle.injection.DefaultInstanceInjectionProvider;

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
