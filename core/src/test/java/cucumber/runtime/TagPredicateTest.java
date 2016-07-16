package cucumber.runtime;

import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class TagPredicateTest {
    private static final String NAME = "pickle_name";
    private static final List<PickleStep> NO_STEPS = Collections.<PickleStep>emptyList();
    private static final PickleLocation MOCK_LOCATION = mock(PickleLocation.class);
    private static final String FOO_TAG_VALUE = "@FOO";
    private static final PickleTag FOO_TAG = new PickleTag(MOCK_LOCATION, FOO_TAG_VALUE);
    private static final String BAR_TAG_VALUE = "@BAR";
    private static final PickleTag BAR_TAG = new PickleTag(MOCK_LOCATION, BAR_TAG_VALUE);
    private static final String NOT_FOO_TAG_VALUE = "~@FOO";
    private static final String FOO_OR_BAR_TAG_VALUE = "@FOO,@BAR";

    @Test
    public void single_tag_predicate_does_not_match_pickle_with_no_tags() {
        Pickle pickle = createPickleWithTags(Collections.<PickleTag>emptyList());
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE));

        assertFalse(predicate.apply(pickle));
    }

    @Test
    public void single_tag_predicate_matches_pickle_with_same_single_tag() {
        Pickle pickle = createPickleWithTags(asList(FOO_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void single_tag_predicate_matches_pickle_with_more_tags() {
        Pickle pickle = createPickleWithTags(asList(FOO_TAG, BAR_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void single_tag_predicate_does_not_match_pickle_with_different_single_tag() {
        Pickle pickle = createPickleWithTags(asList(BAR_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE));

        assertFalse(predicate.apply(pickle));
    }

    @Test
    public void not_tag_predicate_matches_pickle_with_no_tags() {
        Pickle pickle = createPickleWithTags(Collections.<PickleTag>emptyList());
        TagPredicate predicate = new TagPredicate(asList(NOT_FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void not_tag_predicate_does_not_match_pickle_with_same_single_tag() {
        Pickle pickle = createPickleWithTags(asList(FOO_TAG));
        TagPredicate predicate = new TagPredicate(asList(NOT_FOO_TAG_VALUE));

        assertFalse(predicate.apply(pickle));
    }

    @Test
    public void not_tag_predicate_matches_pickle_with_different_single_tag() {
        Pickle pickle = createPickleWithTags(asList(BAR_TAG));
        TagPredicate predicate = new TagPredicate(asList(NOT_FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void and_tag_predicate_matches_pickle_with_all_tags() {
        Pickle pickle = createPickleWithTags(asList(FOO_TAG, BAR_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE, BAR_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void and_tag_predicate_does_not_match_pickle_with_one_of_the_tags() {
        Pickle pickle = createPickleWithTags(asList(FOO_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE, BAR_TAG_VALUE));

        assertFalse(predicate.apply(pickle));
    }

    @Test
    public void or_tag_predicate_matches_pickle_with_one_of_the_tags() {
        Pickle pickle = createPickleWithTags(asList(FOO_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_OR_BAR_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    private Pickle createPickleWithTags(List<PickleTag> tags) {
        return new Pickle(NAME, NO_STEPS, tags, asList(MOCK_LOCATION));
    }
}
