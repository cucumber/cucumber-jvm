package cucumber.runtime.order;

import java.util.List;

import gherkin.events.PickleEvent;

public interface OrderPickleEvents {

	public List<PickleEvent> orderPickleEvents(List<PickleEvent> pickleEvents);
}
