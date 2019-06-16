package io.cucumber.needle;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.ResourceBundle;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LoadCucumberNeedleResourceBundleTest {

    private final LoadResourceBundle function = LoadResourceBundle.INSTANCE;

    @Test
    public void shouldReturnEmptyResourceBundleWhenResourceDoesNotExist() throws Exception {
        final ResourceBundle resourceBundle = function.apply("does-not-exist");
        assertNotNull(resourceBundle);
        assertThat(resourceBundle, CoreMatchers.is(LoadResourceBundle.EMPTY_RESOURCE_BUNDLE));
    }

    @Test
    public void shouldReturnExistingResourceBundle() throws Exception {
        final ResourceBundle resourceBundle = function.apply("empty");
        assertNotNull(resourceBundle);
        assertTrue(resourceBundle.keySet().isEmpty());
    }

    @Test
    public void shouldAlwaysReturnEmptyForEmptyResourceBundle() throws Exception {
        final ResourceBundle resourceBundle = LoadResourceBundle.EMPTY_RESOURCE_BUNDLE;

        assertNotNull(resourceBundle.getObject("foo"));
        assertThat(resourceBundle.getString("foo"), is(""));
        assertFalse(resourceBundle.getKeys().hasMoreElements());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenResourceNameIsNull() {
        function.apply(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenResourceNameIsEmpty() {
        function.apply("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWhenResourceNameIsBlank() {
        function.apply(" ");
    }

}
