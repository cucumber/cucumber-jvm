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
                "  Rule: Test rule\n" +
                "    Scenario Outline: Test scenario\n" +
                "       Given I have 4 <thing> in my belly\n" +
                "       Examples: First\n" +
                "         | thing    | \n" +
                "         | cucumber | \n" +
                "         | gherkin  | \n" +
                "\n" +
                "       Examples: Second\n" +
                "         | thing    | \n" +
                "         | zukini   | \n" +
                "         | pickle   | \n");
    private final Pickle firstPickle = feature.getPickles().get(0);
    private final Pickle secondPickle = feature.getPickles().get(1);
    private final Pickle thirdPickle = feature.getPickles().get(2);
    private final Pickle fourthPickle = feature.getPickles().get(3);

    @Test
    void matches_pickles_from_files_not_in_the_predicate_map() {
        // the argument "path/file.feature another_path/file.feature:8"
        // results in only line predicates only for another_path/file.feature,
        // but all pickles from path/file.feature shall also be executed.
        LinePredicate predicate = new LinePredicate(singletonMap(
            URI.create("classpath:another_path/file.feature"),
            singletonList(8)));
        assertTrue(predicate.test(firstPickle));
    }

    @Test
    void empty() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            emptyList()));
        assertFalse(predicate.test(firstPickle));
        assertFalse(predicate.test(secondPickle));
        assertFalse(predicate.test(thirdPickle));
        assertFalse(predicate.test(fourthPickle));
    }

    @Test
    void matches_at_least_one_line() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            asList(3, 4)));
        assertTrue(predicate.test(firstPickle));
        assertTrue(predicate.test(secondPickle));
        assertTrue(predicate.test(thirdPickle));
        assertTrue(predicate.test(fourthPickle));
    }

    @Test
    void matches_feature() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(1)));
        assertTrue(predicate.test(firstPickle));
        assertTrue(predicate.test(secondPickle));
        assertTrue(predicate.test(thirdPickle));
        assertTrue(predicate.test(fourthPickle));
    }

    @Test
    void matches_rule() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(2)));
        assertTrue(predicate.test(firstPickle));
        assertTrue(predicate.test(secondPickle));
        assertTrue(predicate.test(thirdPickle));
        assertTrue(predicate.test(fourthPickle));
    }

    @Test
    void matches_scenario() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(3)));
        assertTrue(predicate.test(firstPickle));
        assertTrue(predicate.test(secondPickle));
        assertTrue(predicate.test(thirdPickle));
        assertTrue(predicate.test(fourthPickle));
    }

    @Test
    void does_not_match_step() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(4)));
        assertFalse(predicate.test(firstPickle));
        assertFalse(predicate.test(secondPickle));
        assertFalse(predicate.test(thirdPickle));
        assertFalse(predicate.test(fourthPickle));
    }

    @Test
    void matches_first_examples() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(5)));
        assertTrue(predicate.test(firstPickle));
        assertTrue(predicate.test(secondPickle));
        assertFalse(predicate.test(thirdPickle));
        assertFalse(predicate.test(fourthPickle));
    }

    @Test
    void does_not_match_example_header() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(6)));
        assertFalse(predicate.test(firstPickle));
        assertFalse(predicate.test(secondPickle));
        assertFalse(predicate.test(thirdPickle));
        assertFalse(predicate.test(fourthPickle));
    }

    @Test
    void matches_first_example() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(7)));
        assertTrue(predicate.test(firstPickle));
        assertFalse(predicate.test(secondPickle));
        assertFalse(predicate.test(thirdPickle));
        assertFalse(predicate.test(fourthPickle));
    }

    @Test
    void Matches_second_example() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(8)));
        assertFalse(predicate.test(firstPickle));
        assertTrue(predicate.test(secondPickle));
        assertFalse(predicate.test(thirdPickle));
        assertFalse(predicate.test(fourthPickle));
    }

    @Test
    void does_not_match_empty_line() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(9)));
        assertFalse(predicate.test(firstPickle));
        assertFalse(predicate.test(secondPickle));
        assertFalse(predicate.test(thirdPickle));
        assertFalse(predicate.test(fourthPickle));
    }

    @Test
    void matches_second_examples() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(10)));
        assertFalse(predicate.test(firstPickle));
        assertFalse(predicate.test(secondPickle));
        assertTrue(predicate.test(thirdPickle));
        assertTrue(predicate.test(fourthPickle));
    }

    @Test
    void does_not_match_second_examples_header() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(11)));
        assertFalse(predicate.test(firstPickle));
        assertFalse(predicate.test(secondPickle));
        assertFalse(predicate.test(thirdPickle));
        assertFalse(predicate.test(fourthPickle));
    }

    @Test
    void matches_third_example() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(12)));
        assertFalse(predicate.test(firstPickle));
        assertFalse(predicate.test(secondPickle));
        assertTrue(predicate.test(thirdPickle));
        assertFalse(predicate.test(fourthPickle));
    }

    @Test
    void matches_fourth_example() {
        LinePredicate predicate = new LinePredicate(singletonMap(
            featurePath,
            singletonList(13)));
        assertFalse(predicate.test(firstPickle));
        assertFalse(predicate.test(secondPickle));
        assertFalse(predicate.test(thirdPickle));
        assertTrue(predicate.test(fourthPickle));
    }

}
