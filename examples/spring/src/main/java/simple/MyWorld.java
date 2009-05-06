package simple;

import org.springframework.stereotype.Component;

@Component
public class MyWorld implements World {
	
	public String hello() {
		return "Have a cuke, Duke!";
	}
}
