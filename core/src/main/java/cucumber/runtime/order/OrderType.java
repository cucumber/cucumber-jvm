package cucumber.runtime.order;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cucumber.runtime.CucumberException;
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
	};

	private final static Map<String, PickleOrder> orderOptions = createOrderOptions();
	protected final static String DELIMITER = ":";

	private static Map<String, PickleOrder> createOrderOptions() {
		Map<String, PickleOrder> orderOptions = new HashMap<>();
		orderOptions.put("reverse", OrderType.REVERSE);
		orderOptions.put("random", new RandomOrderType());
		return orderOptions;
	}

	public static PickleOrder getPickleOrderType(String details) {

		if(details.endsWith(DELIMITER)) {
			throw new CucumberException("Order Type options cannot end in delimiter.");
		}
		
		String orderName = details.contains(DELIMITER) ? details.split(DELIMITER)[0] : details;
		if (!orderOptions.containsKey(orderName)) {
			throw new CucumberException("Unknown Order Type : " + orderName);
		}
		PickleOrder orderType = orderOptions.get(orderName);
		
		if (orderType instanceof PickleOrderWithOptions) {
			((PickleOrderWithOptions) orderType).populateOrderTypeOptions(details);
		} else if (orderType instanceof PickleOrder) {
			if(details.contains(DELIMITER)) {
				throw new CucumberException("Order Type does not require options. The correct format is '--order "+orderName+"'");
			}
		}
		return orderType;
	}

}