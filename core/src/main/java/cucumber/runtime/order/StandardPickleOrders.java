package cucumber.runtime.order;

import gherkin.events.PickleEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public final class StandardPickleOrders {

    private StandardPickleOrders() {

    }

    public static PickleOrder lexicalUriOrder() {
        return new PickleOrder() {
            @Override
            public List<PickleEvent> orderPickleEvents(List<PickleEvent> pickleEvents) {
                Collections.sort(pickleEvents, new PickleUriComparator());
                return pickleEvents;
            }
        };
    }

    public static PickleOrder reverseLexicalUriOrder() {
        return new PickleOrder() {
            @Override
            public List<PickleEvent> orderPickleEvents(List<PickleEvent> pickleEvents) {
                Collections.sort(pickleEvents, new PickleUriComparator());
                Collections.reverse(pickleEvents);
                return pickleEvents;
            }
        };
    }

    public static PickleOrder random(final long seed) {
        return new PickleOrder() {
            @Override
            public List<PickleEvent> orderPickleEvents(List<PickleEvent> pickleEvents) {
                Collections.shuffle(pickleEvents, new Random(seed));
                return pickleEvents;
            }
        };
    }

    private static class PickleUriComparator implements Comparator<PickleEvent> {

        @Override
        public int compare(PickleEvent a, PickleEvent b) {
            return a.uri.compareTo(b.uri);
        }
    }

}