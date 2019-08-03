package io.cucumber.needle;

import io.cucumber.needle.test.injectionprovider.SimpleNameGetterProvider;
import org.junit.jupiter.api.Test;

import java.util.ResourceBundle;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertNotNull;

public class ReadInjectionProviderClassNamesTest {

    private final ReadInjectionProviderClassNames function = ReadInjectionProviderClassNames.INSTANCE;

    @Test
    public void shouldReturnProviderFromCucumberNeedleProperties() {
        final Set<String> classNames = function.apply(loadBundle(CucumberNeedleConfiguration.RESOURCE_CUCUMBER_NEEDLE));
        assertNotNull(classNames);
        assertThat(classNames.size(), is(1));
        assertThat(classNames.iterator().next(), is(SimpleNameGetterProvider.class.getCanonicalName()));
    }

    @Test
    public void shouldReturnEmptySetWhenResourceBundleIsNull() {
        final Set<String> classNames = function.apply(null);
        assertNotNull(classNames);
        assertThat(classNames.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnEmptySetWhenPropertyIsNotSet() {
        final Set<String> classNames = function.apply(loadBundle("resource-bundles/empty"));
        assertNotNull(classNames);
        assertThat(classNames.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnEmptySetWhenPropertyIsEmpty() {
        final Set<String> classNames = function.apply(loadBundle("resource-bundles/no-classname"));
        assertNotNull(classNames);
        assertThat(classNames.isEmpty(), is(true));
    }

    @Test
    public void shouldReturnOneTrimmedClassName() {
        final Set<String> classNames = function.apply(loadBundle("resource-bundles/one-classname"));
        assertThat(classNames.size(), is(1));
        final String first = classNames.iterator().next();
        assertThat(first, is("java.lang.String"));
    }

    @Test
    public void shouldReturnTwoTrimmedClassNames() {
        final Set<String> classNames = function.apply(loadBundle("resource-bundles/two-classname"));
        assertThat(classNames.size(), is(2));
        assertThat(classNames, hasItems("java.lang.String", "java.util.Set"));
    }

    private ResourceBundle loadBundle(final String resourceName) {
        return LoadResourceBundle.INSTANCE.apply(resourceName);
    }

}
