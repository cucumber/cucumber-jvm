package cucumber.runtime.filter;

import cucumber.messages.Pickles.Pickle;
import org.junit.Test;

import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NamePredicateTest {

    @Test
    public void anchored_name_pattern_matches_exact_name() {
        Pickle pickleEvent = createPickleWithName("a pickle name");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("^a pickle name$")));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void anchored_name_pattern_does_not_match_part_of_name() {
        Pickle pickleEvent = createPickleWithName("a pickle name with suffix");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("^a pickle name$")));

        assertFalse(predicate.apply(pickleEvent));
    }

    @Test
    public void non_anchored_name_pattern_matches_part_of_name() {
        Pickle pickleEvent = createPickleWithName("a pickle name with suffix");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("a pickle name")));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void wildcard_name_pattern_matches_part_of_name() {
        Pickle pickleEvent = createPickleWithName("a pickleEvent name");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("a .* name")));

        assertTrue(predicate.apply(pickleEvent));
    }

    private Pickle createPickleWithName(String pickleName) {
        return Pickle.newBuilder().setName(pickleName).build();
    }
}
