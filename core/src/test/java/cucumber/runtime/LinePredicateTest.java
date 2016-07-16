package cucumber.runtime;

import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinePredicateTest {
    private static final String NAME = "pickle_name";
    private static final List<PickleStep> NO_STEPS = Collections.<PickleStep>emptyList();
    private static final List<PickleTag> NO_TAGS = Collections.<PickleTag>emptyList();

    @Test
    public void matches_pickles_from_files_not_in_the_predicate_map() {
        Pickle pickle = createPickleWithLocations(asList(pickleLocation("path/file.feature", 4)));
        LinePredicate predicate = new LinePredicate(singletonMap("another_path/file.feature", asList(8L)));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void matches_pickles_for_any_line_in_predicate() {
        Pickle pickle = createPickleWithLocations(asList(pickleLocation("path/file.feature", 8)));
        LinePredicate predicate = new LinePredicate(singletonMap("path/file.feature", asList(4L, 8L)));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void matches_pickles_on_any_location_of_the_pickle() {
        Pickle pickle = createPickleWithLocations(asList(pickleLocation("path/file.feature", 4), pickleLocation("path/file.feature", 8)));
        LinePredicate predicate = new LinePredicate(singletonMap("path/file.feature", asList(8L)));

        assertTrue(predicate.apply(pickle));
    }

    @Test
    public void does_not_matches_pickles_not_on_any_line_of_the_predicate() {
        Pickle pickle = createPickleWithLocations(asList(pickleLocation("path/file.feature", 4), pickleLocation("path/file.feature", 8)));
        LinePredicate predicate = new LinePredicate(singletonMap("path/file.feature", asList(10L)));

        assertFalse(predicate.apply(pickle));
    }

    private Pickle createPickleWithLocations(List<PickleLocation> locations) {
        return new Pickle(NAME, NO_STEPS, NO_TAGS, locations);
    }

    private PickleLocation pickleLocation(String path, int line) {
        return new PickleLocation(path, line, 0);
    }


}
