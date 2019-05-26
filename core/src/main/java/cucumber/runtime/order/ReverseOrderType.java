package cucumber.runtime.order;

import java.util.Collections;
import java.util.List;

import gherkin.events.PickleEvent;

public class ReverseOrderType extends OrderType {

	@Override
	public List<PickleEvent> orderPickleEvents(List<PickleEvent> pickleEvents) {	
		Collections.reverse(pickleEvents);
		return pickleEvents;
	}
}
