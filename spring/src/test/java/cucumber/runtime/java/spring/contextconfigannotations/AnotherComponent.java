package cucumber.runtime.java.spring.contextconfigannotations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnotherComponent {

	@Autowired
	private ToInject toInject;
}
