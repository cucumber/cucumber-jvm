package cucumber.runtime.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({MyTest.class, MyTest2.class})
public final class MyTests {
}
