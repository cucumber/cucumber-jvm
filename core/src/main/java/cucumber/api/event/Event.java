package cucumber.api.event;

import java.util.Comparator;

public interface Event {

    //TODO: Doc the exact meaning of this.
    Comparator<Event> CANONICAL_ORDER = new CanonicalEventOrder();

    Long getTimeStamp();

}
