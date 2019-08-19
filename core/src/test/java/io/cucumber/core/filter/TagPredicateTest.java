package io.cucumber.core.filter;

import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.core.feature.TestFeatureParser;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagPredicateTest {

    @Test
    void empty_tag_predicate_matches_pickle_with_any_tags() {
        CucumberPickle pickle = createPickleWithTags("@FOO");
        TagPredicate predicate = new TagPredicate("");
        assertTrue(predicate.test(pickle));
    }


    @Test
    void list_of_empty_tag_predicates_matches_pickle_with_any_tags() {
        CucumberPickle pickle = createPickleWithTags("@FOO");
        TagPredicate predicate = new TagPredicate(asList("", ""));
        assertTrue(predicate.test(pickle));
    }

    @Test
    void single_tag_predicate_does_not_match_pickle_with_no_tags() {
        CucumberPickle pickle = createPickleWithTags();
        TagPredicate predicate = new TagPredicate("@FOO");
        assertFalse(predicate.test(pickle));
    }

    @Test
    void single_tag_predicate_matches_pickle_with_same_single_tag() {
        CucumberPickle pickle = createPickleWithTags("@FOO");
        TagPredicate predicate = new TagPredicate("@FOO");
        assertTrue(predicate.test(pickle));
    }

    @Test
    void single_tag_predicate_matches_pickle_with_more_tags() {
        CucumberPickle pickle = createPickleWithTags("@FOO", "@BAR");
        TagPredicate predicate = new TagPredicate("@FOO");
        assertTrue(predicate.test(pickle));
    }

    @Test
    void single_tag_predicate_does_not_match_pickle_with_different_single_tag() {
        CucumberPickle pickle = createPickleWithTags("@BAR");
        TagPredicate predicate = new TagPredicate("@FOO");
        assertFalse(predicate.test(pickle));
    }

    @Test
    void not_tag_predicate_matches_pickle_with_no_tags() {
        CucumberPickle pickle = createPickleWithTags();
        TagPredicate predicate = new TagPredicate(singletonList("not @FOO"));
        assertTrue(predicate.test(pickle));
    }

    @Test
    void not_tag_predicate_does_not_match_pickle_with_same_single_tag() {
        CucumberPickle pickle = createPickleWithTags("@FOO");
        TagPredicate predicate = new TagPredicate(singletonList("not @FOO"));
        assertFalse(predicate.test(pickle));
    }

    @Test
    void not_tag_predicate_matches_pickle_with_different_single_tag() {
        CucumberPickle pickle = createPickleWithTags("@BAR");
        TagPredicate predicate = new TagPredicate(singletonList("not @FOO"));
        assertTrue(predicate.test(pickle));
    }

    @Test
    void and_tag_predicate_matches_pickle_with_all_tags() {
        CucumberPickle pickle = createPickleWithTags("@FOO", "@BAR");
        TagPredicate predicate = new TagPredicate(singletonList("@FOO and @BAR"));
        assertTrue(predicate.test(pickle));
    }

    @Test
    void and_tag_predicate_does_not_match_pickle_with_one_of_the_tags() {
        CucumberPickle pickle = createPickleWithTags("@FOO");
        TagPredicate predicate = new TagPredicate(singletonList("@FOO and @BAR"));
        assertFalse(predicate.test(pickle));
    }

    @Test
    void or_tag_predicate_matches_pickle_with_one_of_the_tags() {
        CucumberPickle pickle = createPickleWithTags("@FOO");
        TagPredicate predicate = new TagPredicate(singletonList("@FOO or @BAR"));
        assertTrue(predicate.test(pickle));
    }

    @Test
    void or_tag_predicate_does_not_match_pickle_none_of_the_tags() {
        CucumberPickle pickle = createPickleWithTags();
        TagPredicate predicate = new TagPredicate(singletonList("@FOO or @BAR"));
        assertFalse(predicate.test(pickle));
    }

    private CucumberPickle createPickleWithTags(String... tags) {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  " + String.join(" ", tags) + "\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n"
        );
        return feature.getPickles().get(0);
    }

}
