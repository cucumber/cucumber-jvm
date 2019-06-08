package cucumber.runtime.order;

import java.util.List;

import gherkin.events.PickleEvent;

public interface PickleOrder {

	List<PickleEvent> orderPickleEvents(List<PickleEvent> pickleEvents);
}
