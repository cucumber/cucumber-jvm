package cucumber.runtime.order;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import cucumber.runtime.CucumberException;
import gherkin.events.PickleEvent;

public class RandomOrderType implements PickleOrderWithOptions {

	private Long seed = null;	
	private Random random = new Random();
	
	@Override
	public void populateOrderTypeOptions(String orderTypeDetails) {
		if (orderTypeDetails.contains(OrderType.DELIMITER)) {
			String[] details = orderTypeDetails.split(OrderType.DELIMITER);
			if(details.length !=2) {
				throw new CucumberException("The correct format for Random order type with seed is '--order random:<seed>'");
			}
			this.seed = Long.parseLong(details[1]);
		}
	}

	@Override
	public List<PickleEvent> orderPickleEvents(List<PickleEvent> pickleEvents) {
		if(seed == null) {
			seed = random.nextLong();
		}
		random.setSeed(seed);
		System.out.println(String.format("Seed for current RANDOM order execution --> %s", seed));
		
		Collections.shuffle(pickleEvents, random);
		return pickleEvents;
	}

}
