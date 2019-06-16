package io.cucumber.needle;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ResourceBundle;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.jupiter.api.function.Executable;

public class LoadCucumberNeedleResourceBundleTest {

    private final LoadResourceBundle function = LoadResourceBundle.INSTANCE;

    @Test
    public void shouldReturnEmptyResourceBundleWhenResourceDoesNotExist() throws Exception {
        final ResourceBundle resourceBundle = function.apply("does-not-exist");
        assertNotNull(resourceBundle);
        assertThat(resourceBundle, CoreMatchers.is(LoadResourceBundle.EMPTY_RESOURCE_BUNDLE));
    }

    @Test
    public void shouldReturnExistingResourceBundle() {
        final ResourceBundle resourceBundle = function.apply("empty");
        assertNotNull(resourceBundle);
        assertTrue(resourceBundle.keySet().isEmpty());
    }

    @Test
    public void shouldAlwaysReturnEmptyForEmptyResourceBundle() {
        final ResourceBundle resourceBundle = LoadResourceBundle.EMPTY_RESOURCE_BUNDLE;

        assertNotNull(resourceBundle.getObject("foo"));
        assertThat(resourceBundle.getString("foo"), is(""));
        assertFalse(resourceBundle.getKeys().hasMoreElements());
    }

    @Test
    public void shouldFailWhenResourceNameIsNull() {
        final Executable testMethod = () -> function.apply(null);
        final IllegalArgumentException expectedThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("resourceName must not be null or empty!")));
    }

    @Test
    public void shouldFailWhenResourceNameIsEmpty() {
        final Executable testMethod = () -> function.apply("");
        final IllegalArgumentException expectedThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("resourceName must not be null or empty!")));
    }

    @Test
    public void shouldFailWhenResourceNameIsBlank() {
        final Executable testMethod = () -> function.apply(" ");
        final IllegalArgumentException expectedThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("resourceName must not be null or empty!")));
    }

}
