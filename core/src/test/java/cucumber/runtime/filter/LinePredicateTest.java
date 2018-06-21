package cucumber.runtime.filter;

import cucumber.messages.Pickles.Pickle;
import cucumber.messages.Sources.Location;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinePredicateTest {
    @Test
    public void matches_pickles_from_files_not_in_the_predicate_map() {
        // the argument "path/file.feature another_path/file.feature:8"
        // results in only line predicates only for another_path/file.feature,
        // but all pickles from path/file.feature shall also be executed.
        Pickle pickleEvent = createPickleEventWithLocations("path/file.feature", asList(pickleLocation(4)));
        LinePredicate predicate = new LinePredicate(singletonMap("another_path/file.feature", asList(8L)));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void matches_pickles_for_any_line_in_predicate() {
        Pickle pickleEvent = createPickleEventWithLocations("path/file.feature", asList(pickleLocation(8)));
        LinePredicate predicate = new LinePredicate(singletonMap("path/file.feature", asList(4L, 8L)));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void matches_pickles_on_any_location_of_the_pickle() {
        Pickle pickleEvent = createPickleEventWithLocations("path/file.feature", asList(pickleLocation(4), pickleLocation(8)));
        LinePredicate predicate = new LinePredicate(singletonMap("path/file.feature", asList(8L)));

        assertTrue(predicate.apply(pickleEvent));
    }

    @Test
    public void does_not_matches_pickles_not_on_any_line_of_the_predicate() {
        Pickle pickleEvent = createPickleEventWithLocations("path/file.feature", asList(pickleLocation(4), pickleLocation(8)));
        LinePredicate predicate = new LinePredicate(singletonMap("path/file.feature", asList(10L)));

        assertFalse(predicate.apply(pickleEvent));
    }

    private Pickle createPickleEventWithLocations(String uri, List<Location> locations) {
        return Pickle.newBuilder().setUri(uri).addAllLocations(locations).build();
    }

    private Location pickleLocation(int line) {
        return Location.newBuilder().setLine(line).build();
    }


}
