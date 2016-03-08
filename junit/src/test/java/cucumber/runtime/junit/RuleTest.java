package cucumber.runtime.junit;

import cucumber.api.junit.CucumberRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(CucumberRule.ForceRunner.class)
public class RuleTest {
    @ClassRule
    public static final CucumberRule CUCUMBER = new CucumberRule(RuleTest.class.getPackage().getName());
}
