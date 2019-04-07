package cucumber.api.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringClassRuleTestContextManagerAccessor;

import cucumber.runtime.java.spring.SpringFactory;

public class CucumberSpringClassRule extends SpringClassRule {

	private static final Log logger = LogFactory.getLog(CucumberSpringClassRule.class);

	@Override
	public Statement apply(Statement base, Description description) {
		Class<?> testClass = description.getTestClass();
		if (logger.isDebugEnabled()) {
			logger.debug("Applying CucumberSpringClassRule to test class [" + testClass.getName() + "]");
		}
		TestContextManager testContextManager = SpringClassRuleTestContextManagerAccessor.getTestContextManager(testClass);
		SpringFactory.parentContext = testContextManager.getTestContext().getApplicationContext();
		SpringFactory.testClassWithSpringContext = testClass;
		return super.apply(base, description);
	}

}
