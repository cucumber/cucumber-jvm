package cucumber.runtime;

import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class NamePredicateTest {
    private static final List<PickleStep> NO_STEPS = Collections.<PickleStep>emptyList();
    private static final List<PickleTag> NO_TAGS = Collections.<PickleTag>emptyList();
    private static final PickleLocation MOCK_LOCATION = mock(PickleLocation.class);

    @Test
    public void anchored_name_pattern_matches_exact_name() {
        PickleEvent pickleEvent = createPickleWithName("a pickle name");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("^a pickle name$")));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void anchored_name_pattern_does_not_match_part_of_name() {
        PickleEvent pickleEvent = createPickleWithName("a pickle name with suffix");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("^a pickle name$")));

        assertFalse(predicate.apply(pickleEvent));
    }

    @Test
    public void non_anchored_name_pattern_matches_part_of_name() {
        PickleEvent pickleEvent = createPickleWithName("a pickle name with suffix");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("a pickle name")));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void wildcard_name_pattern_matches_part_of_name() {
        PickleEvent pickleEvent = createPickleWithName("a pickleEvent name");
        NamePredicate predicate = new NamePredicate(asList(Pattern.compile("a .* name")));

        assertTrue(predicate.apply(pickleEvent));
    }

    private PickleEvent createPickleWithName(String pickleName) {
        return new PickleEvent("uri", new Pickle(pickleName, "en", NO_STEPS, NO_TAGS, asList(MOCK_LOCATION)));
    }
}
