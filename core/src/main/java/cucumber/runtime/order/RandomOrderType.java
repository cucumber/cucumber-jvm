package cucumber.runtime.order;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import cucumber.runtime.CucumberException;
import gherkin.events.PickleEvent;

public class RandomOrderType extends OrderType {

	private int count;
	
	public RandomOrderType() {}

	public RandomOrderType(Map<String, String> orderTypeData) {
		this(orderTypeData.containsKey(PROPERTY_COUNT) ? 
				Integer.parseInt(orderTypeData.get(PROPERTY_COUNT)) : 0);
	}

	public RandomOrderType(int count) {
		this.count = count;
	}

	@Override
	public List<PickleEvent> orderPickleEvents(List<PickleEvent> pickleEvents) {	
		Collections.shuffle(pickleEvents);
		return pickleEvents.subList(0, ((count > pickleEvents.size() || count == 0) ? 
				pickleEvents.size() : count));
	}
	
	@Override
	public void checkVariableValues(Map<String, String> orderTypeData) {
		if(orderTypeData.containsKey(PROPERTY_COUNT) && 
				Integer.parseInt(orderTypeData.get(PROPERTY_COUNT)) < 1) {
			throw new CucumberException("--count must be > 0");			
		}
	}
}
