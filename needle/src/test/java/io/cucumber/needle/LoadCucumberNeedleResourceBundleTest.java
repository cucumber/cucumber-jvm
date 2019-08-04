package io.cucumber.needle;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ResourceBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LoadCucumberNeedleResourceBundleTest {

    private final LoadResourceBundle function = LoadResourceBundle.INSTANCE;

    @Test
    public void shouldReturnEmptyResourceBundleWhenResourceDoesNotExist() {
        final ResourceBundle resourceBundle = function.apply("does-not-exist");

        assertAll("Checking LoadResourceBundle",
            () -> assertThat(resourceBundle, is(notNullValue())),
            () -> assertThat(resourceBundle, CoreMatchers.is(LoadResourceBundle.EMPTY_RESOURCE_BUNDLE))
        );
    }

    @Test
    public void shouldReturnExistingResourceBundle() {
        final ResourceBundle resourceBundle = function.apply("empty");
        assertThat(resourceBundle, is(notNullValue()));
        assertTrue(resourceBundle.keySet().isEmpty());
    }

    @Test
    public void shouldAlwaysReturnEmptyForEmptyResourceBundle() {
        final ResourceBundle resourceBundle = LoadResourceBundle.EMPTY_RESOURCE_BUNDLE;

        assertAll("Checking ResourceBundle",
            () -> assertThat(resourceBundle.getObject("foo"), is(notNullValue())),
            () -> assertThat(resourceBundle.getString("foo"), is("")),
            () -> assertFalse(resourceBundle.getKeys().hasMoreElements())
        );
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
