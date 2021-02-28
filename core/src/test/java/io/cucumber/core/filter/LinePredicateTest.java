package io.cucumber.core.filter;

import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinePredicateTest {

    public static final URI featurePath = URI.create("classpath:path/file.feature");
    private final Feature feature = TestFeatureParser.parse(
        featurePath,
        "" +
                "Feature: Test feature\n" +
                "  Scenario Outline: Test scenario\n" +
                "     Given I have 4 <thing> in my belly\n" +
                "     Examples:\n" +
                "       | thing    | \n" +
                "       | cucumber | \n" +
                "       | gherkin  | \n");
    private final Pickle pickle = feature.getPickles().get(0);

    @Test
    void matches_pickles_from_files_not_in_the_predicate_map() {
        // the argument "path/file.feature another_path/file.feature:8"
        // results in only line predicates only for another_path/file.feature,
        // but all pickles from path/file.feature shall also be executed.
        LinePredicate predicate = new LinePredicate(singletonMap(
            URI.create("classpath:another_path/file.feature"),
            singletonList(8)));
        assertTrue(predicate.test(pickle));
    }

    @Test
    void does_not_matches_pickles_for_no_lines_in_predicate() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            emptyList()));
        assertFalse(predicate.test(pickle));
    }

    @Test
    void matches_pickles_for_any_line_in_predicate() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            asList(2, 4)));
        assertTrue(predicate.test(pickle));
    }

    @Test
    void matches_pickles_on_scenario_location_of_the_pickle() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(2)));
        assertTrue(predicate.test(pickle));
    }

    @Test
    void matches_pickles_on_example_location_of_the_pickle() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(6)));
        assertTrue(predicate.test(pickle));
    }

    @Test
    void does_not_matches_pickles_not_on_any_line_of_the_predicate() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(4)));
        assertFalse(predicate.test(pickle));
    }

}
