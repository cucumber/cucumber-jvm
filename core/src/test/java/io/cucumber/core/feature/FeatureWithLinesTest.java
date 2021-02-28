package io.cucumber.core.feature;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.emptyCollectionOf;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;

class FeatureWithLinesTest {

    @Test
    void should_create_FileWithFilters_with_no_lines() {
        FeatureWithLines featureWithLines = FeatureWithLines.parse("classpath:example.feature");

        assertAll(
            () -> assertThat(featureWithLines.uri(), is(URI.create("classpath:example.feature"))),
            () -> assertThat(featureWithLines.lines(), emptyCollectionOf(Integer.class)));
    }

    @Test
    void should_create_FileWithFilters_with_1_line() {
        FeatureWithLines featureWithLines = FeatureWithLines.parse("classpath:example.feature:999");

        assertAll(
            () -> assertThat(featureWithLines.uri(), is(URI.create("classpath:example.feature"))),
            () -> assertThat(featureWithLines.lines(), contains(999)));
    }

    @Test
    void should_create_FileWithFilters_with_2_lines() {
        FeatureWithLines featureWithLines = FeatureWithLines.parse("classpath:example.feature:999:2000");

        assertAll(
            () -> assertThat(featureWithLines.uri(), is(URI.create("classpath:example.feature"))),
            () -> assertThat(featureWithLines.lines(), contains(999, 2000)));
    }

}
