package io.cucumber.core.model;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.collection.IsEmptyCollection.emptyCollectionOf;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class FeatureWithLinesTest {
    @Test
    public void should_create_FileWithFilters_with_no_lines() {
        FeatureWithLines featureWithLines = FeatureWithLines.parse("foo.feature");
        assertEquals(URI.create("file:foo.feature"), featureWithLines.uri());
        assertThat(featureWithLines.lines(), emptyCollectionOf(Integer.class));
    }

    @Test
    public void should_create_FileWithFilters_with_1_line() {
        FeatureWithLines featureWithLines = FeatureWithLines.parse("foo.feature:999");
        assertEquals(URI.create("file:foo.feature"), featureWithLines.uri());
        assertThat(featureWithLines.lines(), contains(999));
    }

    @Test
    public void should_create_FileWithFilters_with_2_lines() {
        FeatureWithLines featureWithLines = FeatureWithLines.parse("foo.feature:999:2000");
        assertEquals(URI.create("file:foo.feature"), featureWithLines.uri());
        assertThat(featureWithLines.lines(), contains(999, 2000));
    }
}
