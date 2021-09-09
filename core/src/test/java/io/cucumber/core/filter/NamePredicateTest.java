package io.cucumber.core.filter;

import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamePredicateTest {

    @Test
    void anchored_name_pattern_matches_exact_name() {
        Pickle pickle = createPickleWithName("a pickle name");
        NamePredicate predicate = new NamePredicate(singletonList(Pattern.compile("^a pickle name$")));

        assertTrue(predicate.test(pickle));
    }

    private Pickle createPickleWithName(String pickleName) {
        Feature feature = TestFeatureParser.parse("file:path/file.feature", "" +
                "Feature: Test feature\n" +
                "  Scenario: " + pickleName + "\n" +
                "     Given I have 4 cukes in my belly\n");
        return feature.getPickles().get(0);
    }

    @Test
    void anchored_name_pattern_does_not_match_part_of_name() {
        Pickle pickle = createPickleWithName("a pickle name with suffix");
        NamePredicate predicate = new NamePredicate(singletonList(Pattern.compile("^a pickle name$")));

        assertFalse(predicate.test(pickle));
    }

    @Test
    void non_anchored_name_pattern_matches_part_of_name() {
        Pickle pickle = createPickleWithName("a pickle name with suffix");
        NamePredicate predicate = new NamePredicate(singletonList(Pattern.compile("a pickle name")));

        assertTrue(predicate.test(pickle));
    }

    @Test
    void wildcard_name_pattern_matches_part_of_name() {
        Pickle pickle = createPickleWithName("a pickle name");
        NamePredicate predicate = new NamePredicate(singletonList(Pattern.compile("a .* name")));

        assertTrue(predicate.test(pickle));
    }

}
