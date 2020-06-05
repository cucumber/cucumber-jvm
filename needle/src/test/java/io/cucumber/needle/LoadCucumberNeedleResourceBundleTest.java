package io.cucumber.needle;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ResourceBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadCucumberNeedleResourceBundleTest {

    private final LoadResourceBundle function = LoadResourceBundle.INSTANCE;

    @Test
    void shouldReturnEmptyResourceBundleWhenResourceDoesNotExist() {
        final ResourceBundle resourceBundle = function.apply("does-not-exist");

        assertAll(
            () -> assertThat(resourceBundle, is(notNullValue())),
            () -> assertThat(resourceBundle, CoreMatchers.is(LoadResourceBundle.EMPTY_RESOURCE_BUNDLE)));
    }

    @Test
    void shouldReturnExistingResourceBundle() {
        final ResourceBundle resourceBundle = function.apply("empty");
        assertThat(resourceBundle, is(notNullValue()));
        assertTrue(resourceBundle.keySet().isEmpty());
    }

    @Test
    void shouldAlwaysReturnEmptyForEmptyResourceBundle() {
        final ResourceBundle resourceBundle = LoadResourceBundle.EMPTY_RESOURCE_BUNDLE;

        assertAll(
            () -> assertThat(resourceBundle.getObject("foo"), is(notNullValue())),
            () -> assertThat(resourceBundle.getString("foo"), is("")),
            () -> assertFalse(resourceBundle.getKeys().hasMoreElements()));
    }

    @Test
    void shouldFailWhenResourceNameIsNull() {
        Executable testMethod = () -> function.apply(null);
        IllegalArgumentException expectedThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("resourceName must not be null or empty!")));
    }

    @Test
    void shouldFailWhenResourceNameIsEmpty() {
        Executable testMethod = () -> function.apply("");
        IllegalArgumentException expectedThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("resourceName must not be null or empty!")));
    }

    @Test
    void shouldFailWhenResourceNameIsBlank() {
        Executable testMethod = () -> function.apply(" ");
        IllegalArgumentException expectedThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("resourceName must not be null or empty!")));
    }

}
