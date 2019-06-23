package io.cucumber.core.feature;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class FeatureIdentifierTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void can_parse_feature_path_with_feature(){
        URI uri = FeatureIdentifier.parse(FeaturePath.parse("classpath:/path/to/file.feature"));
        assertEquals("classpath", uri.getScheme());
        assertEquals("/path/to/file.feature", uri.getSchemeSpecificPart());
    }

    @Test
    public void reject_feature_with_lines(){
        expectedException.expectMessage("featureIdentifier does not reference a single feature file");
        FeatureIdentifier.parse(URI.create("classpath:/path/to/file.feature:10:40"));
    }

    @Test
    public void reject_directory_form(){
        expectedException.expectMessage("featureIdentifier does not reference a single feature file");
        FeatureIdentifier.parse(URI.create("classpath:/path/to"));
    }
}
