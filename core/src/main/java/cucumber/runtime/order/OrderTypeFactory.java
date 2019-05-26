package cucumber.runtime.order;

import java.util.Map;

import cucumber.runtime.CucumberException;

public class OrderTypeFactory {

	public static OrderType getOrderType(Map<String, String> orderTypeData) {
		String type = orderTypeData.get(OrderType.TYPE_NAME);
		OrderType order = null;
		
		if(type.equalsIgnoreCase(OrderType.RANDOM_ORDER_TYPE))
			order = new RandomOrderType(orderTypeData);
		else if(type.equalsIgnoreCase(OrderType.REVERSE_ORDER_TYPE))
			order = new ReverseOrderType();
		else if(type.equalsIgnoreCase(OrderType.NONE_ORDER_TYPE))
			order = new NoneOrderType();
		else
			throw new CucumberException("OrderType '"+ type + "' is not supported.");
		
		return order;
	}
	
	public static OrderType createNoneOrderType() {
		return new NoneOrderType();
	}
}
