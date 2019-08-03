package io.cucumber.core.feature;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FeatureIdentifierTest {

    @Test
    public void can_parse_feature_path_with_feature() {
        URI uri = FeatureIdentifier.parse(FeaturePath.parse("classpath:/path/to/file.feature"));
        assertEquals("classpath", uri.getScheme());
        assertEquals("/path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void reject_feature_with_lines() {
        final Executable testMethod = () -> FeatureIdentifier.parse(URI.create("classpath:/path/to/file.feature:10:40"));
        final IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "featureIdentifier does not reference a single feature file: classpath:/path/to/file.feature:10:40"
        )));
    }

    @Test
    public void reject_directory_form() {
        final Executable testMethod = () -> FeatureIdentifier.parse(URI.create("classpath:/path/to"));
        final IllegalArgumentException actualThrown = assertThrows(IllegalArgumentException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "featureIdentifier does not reference a single feature file: classpath:/path/to"
        )));
    }

}
