package cucumber.runtime.order;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import gherkin.events.PickleEvent;

public class RandomOrderTypeTest {
    
    @Test
    public void count_greater_than_filtered_pickles_size() {
    	List<PickleEvent> pickles = createFilteredPickleList(5);
    	Map<String, String> orderTypeData = createOrderTypeData(3);
    	RandomOrderType random = new RandomOrderType(orderTypeData);
    	assertThat(random.orderPickleEvents(pickles).size(), is(3));
    }
    
    @Test
    public void count_less_than_filtered_pickles_size() {
    	List<PickleEvent> pickles = createFilteredPickleList(5);
    	Map<String, String> orderTypeData = createOrderTypeData(7);
    	RandomOrderType random = new RandomOrderType(orderTypeData);
    	assertThat(random.orderPickleEvents(pickles).size(), is(5));
    }
    
    @Test
    public void count_property_not_specified() {
    	List<PickleEvent> pickles = createFilteredPickleList(5);
    	Map<String, String> orderTypeData = new HashMap<>();
    	orderTypeData.put(OrderType.TYPE_NAME, OrderType.RANDOM_ORDER_TYPE);
    	RandomOrderType random = new RandomOrderType(orderTypeData);
    	assertThat(random.orderPickleEvents(pickles).size(), is(5));
    }

	private Map<String, String> createOrderTypeData(int count) {
		Map<String, String> orderTypeData = new HashMap<>();
    	orderTypeData.put(OrderType.TYPE_NAME, OrderType.RANDOM_ORDER_TYPE);
    	orderTypeData.put(OrderType.PROPERTY_COUNT, String.valueOf(count));
		return orderTypeData;
	}
    
    private List<PickleEvent> createFilteredPickleList(int size) {    	
    	List<PickleEvent> pickles = new ArrayList<>();    	
    	for(int i=0; i<size; i++) {
    		pickles.add(mock(PickleEvent.class));
    	}
    	return pickles;
    }

}
