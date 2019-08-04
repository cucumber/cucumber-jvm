package io.cucumber.core.feature;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.emptyCollectionOf;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

public class FeatureWithLinesTest {

    @Test
    public void should_create_FileWithFilters_with_no_lines() {
        FeatureWithLines featureWithLines = FeatureWithLines.parse("foo.feature");

        assertAll("Checking FeatureWithLines",
            () -> assertThat(featureWithLines.uri(), is(equalTo(URI.create("file:foo.feature")))),
            () -> assertThat(featureWithLines.lines(), emptyCollectionOf(Integer.class))
        );
    }

    @Test
    public void should_create_FileWithFilters_with_1_line() {
        FeatureWithLines featureWithLines = FeatureWithLines.parse("foo.feature:999");

        assertAll("Checking FeatureWithLines",
            () -> assertThat(featureWithLines.uri(), is(equalTo(URI.create("file:foo.feature")))),
            () -> assertThat(featureWithLines.lines(), contains(999))
        );
    }

    @Test
    public void should_create_FileWithFilters_with_2_lines() {
        FeatureWithLines featureWithLines = FeatureWithLines.parse("foo.feature:999:2000");

        assertAll("Checking FeatureWithLines",
            () -> assertThat(featureWithLines.uri(), is(equalTo(URI.create("file:foo.feature")))),
            () -> assertThat(featureWithLines.lines(), contains(999, 2000))
        );
    }

}
