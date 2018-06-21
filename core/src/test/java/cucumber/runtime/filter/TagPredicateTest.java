package cucumber.runtime.filter;

import cucumber.messages.Pickles.Pickle;
import cucumber.messages.Pickles.PickleTag;
import org.junit.Test;

import static cucumber.runtime.PickleHelper.pickle;
import static cucumber.runtime.PickleHelper.pickleWithTags;
import static cucumber.runtime.PickleHelper.tag;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TagPredicateTest {
    private static final String FOO_TAG_VALUE = "@FOO";
    private static final PickleTag FOO_TAG = tag(FOO_TAG_VALUE);
    private static final String BAR_TAG_VALUE = "@BAR";
    private static final PickleTag BAR_TAG = tag(BAR_TAG_VALUE);
    private static final String NOT_FOO_TAG_VALUE = "not @FOO";
    private static final String FOO_OR_BAR_TAG_VALUE = "@FOO or @BAR";
    private static final String FOO_AND_BAR_TAG_VALUE = "@FOO and @BAR";
    private static final String OLD_STYLE_NOT_FOO_TAG_VALUE = "~@FOO";
    private static final String OLD_STYLE_FOO_OR_BAR_TAG_VALUE = "@FOO,@BAR";

    @Test
    public void empty_tag_predicate_matches_pickle_with_any_tags() {
        Pickle pickle = pickleWithTags(FOO_TAG);
        TagPredicate predicate = new TagPredicate(null);

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void single_tag_predicate_does_not_match_pickle_with_no_tags() {
        Pickle pickle = pickle();
        TagPredicate predicate = new TagPredicate(singletonList(FOO_TAG_VALUE));

        assertFalse(predicate.apply(pickle));
    }

    @Test
    public void single_tag_predicate_matches_pickle_with_same_single_tag() {
        Pickle pickle = pickleWithTags(FOO_TAG);
        TagPredicate predicate = new TagPredicate(singletonList(FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void single_tag_predicate_matches_pickle_with_more_tags() {
        Pickle pickle = pickleWithTags(FOO_TAG, BAR_TAG);
        TagPredicate predicate = new TagPredicate(singletonList(FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void single_tag_predicate_does_not_match_pickle_with_different_single_tag() {
        Pickle pickle = pickleWithTags(BAR_TAG);
        TagPredicate predicate = new TagPredicate(singletonList(FOO_TAG_VALUE));

        assertFalse(predicate.apply(pickle));
    }

    @Test
    public void not_tag_predicate_matches_pickle_with_no_tags() {
        Pickle pickle = pickle();
        TagPredicate predicate = new TagPredicate(singletonList(NOT_FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void not_tag_predicate_does_not_match_pickle_with_same_single_tag() {
        Pickle pickle = pickleWithTags(FOO_TAG);
        TagPredicate predicate = new TagPredicate(singletonList(NOT_FOO_TAG_VALUE));

        assertFalse(predicate.apply(pickle));
    }

    @Test
    public void not_tag_predicate_matches_pickle_with_different_single_tag() {
        Pickle pickle = pickleWithTags(BAR_TAG);
        TagPredicate predicate = new TagPredicate(singletonList(NOT_FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void and_tag_predicate_matches_pickle_with_all_tags() {
        Pickle pickle = pickleWithTags(FOO_TAG, BAR_TAG);
        TagPredicate predicate = new TagPredicate(singletonList(FOO_AND_BAR_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void and_tag_predicate_does_not_match_pickle_with_one_of_the_tags() {
        Pickle pickle = pickleWithTags(FOO_TAG);
        TagPredicate predicate = new TagPredicate(singletonList(FOO_AND_BAR_TAG_VALUE));

        assertFalse(predicate.apply(pickle));
    }

    @Test
    public void or_tag_predicate_matches_pickle_with_one_of_the_tags() {
        Pickle pickle = pickleWithTags(FOO_TAG);
        TagPredicate predicate = new TagPredicate(singletonList(FOO_OR_BAR_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void or_tag_predicate_does_not_match_pickle_none_of_the_tags() {
        Pickle pickle = pickle();
        TagPredicate predicate = new TagPredicate(singletonList(FOO_OR_BAR_TAG_VALUE));

        assertFalse(predicate.apply(pickle));
    }

    @Test
    public void old_style_not_tag_predicate_is_handled() {
        Pickle pickle = pickleWithTags(BAR_TAG);
        TagPredicate predicate = new TagPredicate(singletonList(OLD_STYLE_NOT_FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void old_style_or_tag_predicate_is_handled() {
        Pickle pickle = pickleWithTags(FOO_TAG);
        TagPredicate predicate = new TagPredicate(singletonList(OLD_STYLE_FOO_OR_BAR_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void multiple_tag_expressions_are_combined_with_and() {
        Pickle pickle = pickleWithTags(FOO_TAG, BAR_TAG);
        TagPredicate predicate = new TagPredicate(asList(FOO_TAG_VALUE, BAR_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void old_and_new_style_tag_expressions_can_be_combined() {
        Pickle pickle = pickleWithTags(BAR_TAG);
        TagPredicate predicate = new TagPredicate(asList(BAR_TAG_VALUE, OLD_STYLE_NOT_FOO_TAG_VALUE));

        assertTrue(predicate.apply(pickle));
    }

}
