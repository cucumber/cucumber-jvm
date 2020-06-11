package io.cucumber.core.filter;

import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.tagexpressions.TagExpressionParser;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagPredicateTest {

    @Test
    void empty_tag_predicate_matches_pickle_with_any_tags() {
        Pickle pickle = createPickleWithTags("@FOO");
        TagPredicate predicate = createPredicate("");
        assertTrue(predicate.test(pickle));
    }

    @Test
    void list_of_empty_tag_predicates_matches_pickle_with_any_tags() {
        Pickle pickle = createPickleWithTags("@FOO");
        TagPredicate predicate = createPredicate("", "");
        assertTrue(predicate.test(pickle));
    }

    @Test
    void single_tag_predicate_does_not_match_pickle_with_no_tags() {
        Pickle pickle = createPickleWithTags();
        TagPredicate predicate = createPredicate("@FOO");
        assertFalse(predicate.test(pickle));
    }

    @Test
    void single_tag_predicate_matches_pickle_with_same_single_tag() {
        Pickle pickle = createPickleWithTags("@FOO");
        TagPredicate predicate = createPredicate("@FOO");
        assertTrue(predicate.test(pickle));
    }

    @Test
    void single_tag_predicate_matches_pickle_with_more_tags() {
        Pickle pickle = createPickleWithTags("@FOO", "@BAR");
        TagPredicate predicate = createPredicate("@FOO");
        assertTrue(predicate.test(pickle));
    }

    @Test
    void single_tag_predicate_does_not_match_pickle_with_different_single_tag() {
        Pickle pickle = createPickleWithTags("@BAR");
        TagPredicate predicate = createPredicate("@FOO");
        assertFalse(predicate.test(pickle));
    }

    @Test
    void not_tag_predicate_matches_pickle_with_no_tags() {
        Pickle pickle = createPickleWithTags();
        TagPredicate predicate = createPredicate("not @FOO");
        assertTrue(predicate.test(pickle));
    }

    @Test
    void not_tag_predicate_does_not_match_pickle_with_same_single_tag() {
        Pickle pickle = createPickleWithTags("@FOO");
        TagPredicate predicate = createPredicate("not @FOO");
        assertFalse(predicate.test(pickle));
    }

    @Test
    void not_tag_predicate_matches_pickle_with_different_single_tag() {
        Pickle pickle = createPickleWithTags("@BAR");
        TagPredicate predicate = createPredicate("not @FOO");
        assertTrue(predicate.test(pickle));
    }

    @Test
    void and_tag_predicate_matches_pickle_with_all_tags() {
        Pickle pickle = createPickleWithTags("@FOO", "@BAR");
        TagPredicate predicate = createPredicate("@FOO and @BAR");
        assertTrue(predicate.test(pickle));
    }

    @Test
    void and_tag_predicate_does_not_match_pickle_with_one_of_the_tags() {
        Pickle pickle = createPickleWithTags("@FOO");
        TagPredicate predicate = createPredicate("@FOO and @BAR");
        assertFalse(predicate.test(pickle));
    }

    @Test
    void or_tag_predicate_matches_pickle_with_one_of_the_tags() {
        Pickle pickle = createPickleWithTags("@FOO");
        TagPredicate predicate = createPredicate("@FOO or @BAR");
        assertTrue(predicate.test(pickle));
    }

    @Test
    void or_tag_predicate_does_not_match_pickle_none_of_the_tags() {
        Pickle pickle = createPickleWithTags();
        TagPredicate predicate = createPredicate("@FOO or @BAR");
        assertFalse(predicate.test(pickle));
    }

    private static Pickle createPickleWithTags(String... tags) {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  " + String.join(" ", tags) + "\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");
        return feature.getPickles().get(0);
    }

    private static TagPredicate createPredicate(String... expressions) {
        return new TagPredicate(stream(expressions)
                .map(TagExpressionParser::parse)
                .collect(Collectors.toList()));
    }
}
