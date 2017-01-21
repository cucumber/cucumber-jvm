package cucumber.runtime;

import gherkin.events.PickleEvent;
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
    private static final String LANGUAGE = "en";
    private static final List<PickleStep> NO_STEPS = Collections.<PickleStep>emptyList();
    private static final PickleLocation MOCK_LOCATION = mock(PickleLocation.class);
    private static final String FOO_TAG_VALUE = "@FOO";
    private static final PickleTag FOO_TAG = new PickleTag(MOCK_LOCATION, FOO_TAG_VALUE);
    private static final String BAR_TAG_VALUE = "@BAR";
    private static final PickleTag BAR_TAG = new PickleTag(MOCK_LOCATION, BAR_TAG_VALUE);
    private static final String NOT_FOO_TAG_VALUE = "not @FOO";
    private static final String FOO_OR_BAR_TAG_VALUE = "@FOO or @BAR";
    private static final String FOO_AND_BAR_TAG_VALUE = "@FOO and @BAR";
    private static final String OLD_STYLE_NOT_FOO_TAG_VALUE = "~@FOO";
    private static final String OLD_STYLE_FOO_OR_BAR_TAG_VALUE = "@FOO,@BAR";

    @Test
    public void empty_tag_predicate_matches_pickle_with_any_tags() {
        PickleEvent pickleEvent = createPickleWithTags(asList(FOO_TAG));
        TagPredicate predicate = new TagPredicate(null);

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void single_tag_predicate_does_not_match_pickle_with_no_tags() {
        PickleEvent pickleEvent = createPickleWithTags(Collections.<PickleTag>emptyList());
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE));

        assertFalse(predicate.apply(pickleEvent));
    }

    @Test
    public void single_tag_predicate_matches_pickle_with_same_single_tag() {
        PickleEvent pickleEvent = createPickleWithTags(asList(FOO_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void single_tag_predicate_matches_pickle_with_more_tags() {
        PickleEvent pickleEvent = createPickleWithTags(asList(FOO_TAG, BAR_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void single_tag_predicate_does_not_match_pickle_with_different_single_tag() {
        PickleEvent pickleEvent = createPickleWithTags(asList(BAR_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE));

        assertFalse(predicate.apply(pickleEvent));
    }

    @Test
    public void not_tag_predicate_matches_pickle_with_no_tags() {
        PickleEvent pickleEvent = createPickleWithTags(Collections.<PickleTag>emptyList());
        TagPredicate predicate = new TagPredicate(asList(NOT_FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void not_tag_predicate_does_not_match_pickle_with_same_single_tag() {
        PickleEvent pickleEvent = createPickleWithTags(asList(FOO_TAG));
        TagPredicate predicate = new TagPredicate(asList(NOT_FOO_TAG_VALUE));

        assertFalse(predicate.apply(pickleEvent));
    }

    @Test
    public void not_tag_predicate_matches_pickle_with_different_single_tag() {
        PickleEvent pickleEvent = createPickleWithTags(asList(BAR_TAG));
        TagPredicate predicate = new TagPredicate(asList(NOT_FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void and_tag_predicate_matches_pickle_with_all_tags() {
        PickleEvent pickleEvent = createPickleWithTags(asList(FOO_TAG, BAR_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_AND_BAR_TAG_VALUE));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void and_tag_predicate_does_not_match_pickle_with_one_of_the_tags() {
        PickleEvent pickleEvent = createPickleWithTags(asList(FOO_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_AND_BAR_TAG_VALUE));

        assertFalse(predicate.apply(pickleEvent));
    }

    @Test
    public void or_tag_predicate_matches_pickle_with_one_of_the_tags() {
        PickleEvent pickleEvent = createPickleWithTags(asList(FOO_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_OR_BAR_TAG_VALUE));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void or_tag_predicate_does_not_match_pickle_none_of_the_tags() {
        PickleEvent pickleEvent = createPickleWithTags(Collections.<PickleTag>emptyList());
        TagPredicate predicate = new TagPredicate(asList(FOO_OR_BAR_TAG_VALUE));

        assertFalse(predicate.apply(pickleEvent));
    }

    @Test
    public void old_style_not_tag_predicate_is_handled() {
        PickleEvent pickleEvent = createPickleWithTags(asList(BAR_TAG));
        TagPredicate predicate = new TagPredicate(asList(OLD_STYLE_NOT_FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void old_style_or_tag_predicate_is_handled() {
        PickleEvent pickleEvent = createPickleWithTags(asList(FOO_TAG));
        TagPredicate predicate = new TagPredicate(asList(OLD_STYLE_FOO_OR_BAR_TAG_VALUE));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void multiple_tag_expressions_are_combined_with_and() {
        PickleEvent pickleEvent = createPickleWithTags(asList(FOO_TAG, BAR_TAG));
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE, BAR_TAG_VALUE));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void old_and_new_style_tag_expressions_can_be_combined() {
        PickleEvent pickleEvent = createPickleWithTags(asList(BAR_TAG));
        TagPredicate predicate = new TagPredicate(asList(BAR_TAG_VALUE, OLD_STYLE_NOT_FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickleEvent));
    }

    private PickleEvent createPickleWithTags(List<PickleTag> tags) {
        return new PickleEvent("uri", new Pickle(NAME, LANGUAGE, NO_STEPS, tags, asList(MOCK_LOCATION)));
    }
}
