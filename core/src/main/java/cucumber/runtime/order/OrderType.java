package cucumber.runtime.order;

import java.util.List;
import java.util.Map;

import gherkin.events.PickleEvent;

public abstract class OrderType {

	public static final String NONE_ORDER_TYPE = "None";
	
	public static final String RANDOM_ORDER_TYPE = "Random";
		
	public static final String REVERSE_ORDER_TYPE = "Reverse";
	
	public static final String TYPE_NAME = "type";
	
	public static final String PROPERTY_COUNT = "count";
	
	public List<PickleEvent> orderPickleEvents(List<PickleEvent> filteredPickleEvents) {
		return filteredPickleEvents;
	}
	
	public void checkVariableValues(Map<String, String> orderTypeData) {}
		
}
