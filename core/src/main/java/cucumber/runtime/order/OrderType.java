package cucumber.runtime.order;

 import java.util.Collections;
import java.util.List;

import gherkin.events.PickleEvent;

 public enum OrderType implements PickleOrder {

 	NONE {
		@Override
		public List<PickleEvent> orderPickleEvents(List<PickleEvent> pickleEvents) {
			return pickleEvents;
		}
	},
	REVERSE {
		@Override
		public List<PickleEvent> orderPickleEvents(List<PickleEvent> pickleEvents) {
			Collections.reverse(pickleEvents);
			return pickleEvents;
		}
	},
	RANDOM {
		@Override
		public List<PickleEvent> orderPickleEvents(List<PickleEvent> pickleEvents) {
			Collections.shuffle(pickleEvents);
			return pickleEvents;
		}		
	}

 }