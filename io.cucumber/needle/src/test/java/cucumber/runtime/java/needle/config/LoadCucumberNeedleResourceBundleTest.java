package cucumber.runtime.java.needle.config;

import static cucumber.runtime.java.needle.config.LoadResourceBundle.EMPTY_RESOURCE_BUNDLE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ResourceBundle;

import org.junit.Test;

public class LoadCucumberNeedleResourceBundleTest {

    private final LoadResourceBundle function = LoadResourceBundle.INSTANCE;

    @Test
    public void shouldReturnEmptyResourceBundleWhenResourceDoesNotExist() throws Exception {
        final ResourceBundle resourceBundle = function.apply("does-not-exist");
        assertNotNull(resourceBundle);
        assertThat(resourceBundle, is(EMPTY_RESOURCE_BUNDLE));
    }

    @Test
    public void shouldReturnExistingResourceBundle() throws Exception {
        final ResourceBundle resourceBundle = function.apply("empty");
        assertNotNull(resourceBundle);
        assertTrue(resourceBundle.keySet().isEmpty());
    }

    @Test
    public void shouldAlwaysReturnEmptyForEmptyResourceBundle() throws Exception {
        final ResourceBundle resourceBundle = EMPTY_RESOURCE_BUNDLE;

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
