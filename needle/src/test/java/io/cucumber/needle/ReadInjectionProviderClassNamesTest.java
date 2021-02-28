package io.cucumber.needle;

import io.cucumber.needle.test.injectionprovider.SimpleNameGetterProvider;
import org.junit.jupiter.api.Test;

import java.util.ResourceBundle;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsIterableContaining.hasItems;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReadInjectionProviderClassNamesTest {

    private final ReadInjectionProviderClassNames function = ReadInjectionProviderClassNames.INSTANCE;

    @Test
    void shouldReturnProviderFromCucumberNeedleProperties() {
        final Set<String> classNames = function.apply(loadBundle(CucumberNeedleConfiguration.RESOURCE_CUCUMBER_NEEDLE));
        assertThat(classNames, is(notNullValue()));
        assertThat(classNames.size(), is(1));
        assertThat(classNames.iterator().next(), is(SimpleNameGetterProvider.class.getCanonicalName()));
    }

    private ResourceBundle loadBundle(final String resourceName) {
        return LoadResourceBundle.INSTANCE.apply(resourceName);
    }

    @Test
    void shouldReturnEmptySetWhenResourceBundleIsNull() {
        final Set<String> classNames = function.apply(null);
        assertThat(classNames, is(notNullValue()));
        assertThat(classNames.isEmpty(), is(true));
    }

    @Test
    void shouldReturnEmptySetWhenPropertyIsNotSet() {
        final Set<String> classNames = function.apply(loadBundle("resource-bundles/empty"));
        assertThat(classNames, is(notNullValue()));
        assertThat(classNames.isEmpty(), is(true));
    }

    @Test
    void shouldReturnEmptySetWhenPropertyIsEmpty() {
        final Set<String> classNames = function.apply(loadBundle("resource-bundles/no-classname"));
        assertThat(classNames, is(notNullValue()));
        assertThat(classNames.isEmpty(), is(true));
    }

    @Test
    void shouldReturnOneTrimmedClassName() {
        final Set<String> classNames = function.apply(loadBundle("resource-bundles/one-classname"));
        assertThat(classNames.size(), is(1));
        final String first = classNames.iterator().next();
        assertThat(first, is("java.lang.String"));
    }

    @Test
    void shouldReturnTwoTrimmedClassNames() {
        final Set<String> classNames = function.apply(loadBundle("resource-bundles/two-classname"));

        assertAll(
            () -> assertThat(classNames.size(), is(2)),
            () -> assertThat(classNames, hasItems("java.lang.String", "java.util.Set")));
    }

}
