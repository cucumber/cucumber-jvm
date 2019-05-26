package cucumber.runtime.order;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cucumber.runtime.CucumberException;

public class OrderTypeFactoryTest {
	
	@Rule
    public ExpectedException expectedException = ExpectedException.none();
	
	@Test
	public void create_none_ordertype() {
		Map<String, String> orderTypeData = new HashMap<>();
		orderTypeData.put(OrderType.TYPE_NAME, OrderType.NONE_ORDER_TYPE);
		assertThat(OrderTypeFactory.getOrderType(orderTypeData), instanceOf(NoneOrderType.class));
	}
	
	@Test
	public void create_random_ordertype() {
		Map<String, String> orderTypeData = new HashMap<>();
		orderTypeData.put(OrderType.TYPE_NAME, OrderType.RANDOM_ORDER_TYPE);
		assertThat(OrderTypeFactory.getOrderType(orderTypeData), instanceOf(RandomOrderType.class));
	}
	
	@Test
	public void create_reverse_ordertype() {
		Map<String, String> orderTypeData = new HashMap<>();
		orderTypeData.put(OrderType.TYPE_NAME, OrderType.REVERSE_ORDER_TYPE);
		assertThat(OrderTypeFactory.getOrderType(orderTypeData), instanceOf(ReverseOrderType.class));
	}
	
	@Test
	public void create_invalid_ordertype() {
		Map<String, String> orderTypeData = new HashMap<>();
		String type = "Invalid";
		orderTypeData.put(OrderType.TYPE_NAME, type);
        expectedException.expect(CucumberException.class);
        expectedException.expectMessage("OrderType '"+ type + "' is not supported.");
        OrderTypeFactory.getOrderType(orderTypeData);
	}

}
