package cucumber.runtime.junit;

import cucumber.api.junit.CucumberRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(CucumberRule.NoTestRunner.class) // just cause with a test junit runner would fail so this one allows to run such tests
public class RuleTest {
    @ClassRule
    public static final CucumberRule CUCUMBER = new CucumberRule().glue(RuleTest.class.getPackage());
}
