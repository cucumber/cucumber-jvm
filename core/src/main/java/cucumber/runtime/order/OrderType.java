package cucumber.runtime.order;

 import java.util.Collections;
import java.util.List;

 import cucumber.runtime.CucumberException;
import gherkin.events.PickleEvent;

 public enum OrderType implements OrderPickleEvents {

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
	};


     public static OrderType getOrderType(String name) {
        for (OrderType orderType : OrderType.values()) {
            if (name.equalsIgnoreCase(orderType.name())) {
                return orderType;
            }
        }
        throw new CucumberException(String.format("Unrecognized OrderType %s", name));
    }
 }