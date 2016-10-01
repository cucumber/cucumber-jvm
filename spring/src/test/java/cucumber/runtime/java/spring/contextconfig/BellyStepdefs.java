package cucumber.runtime.java.spring.contextconfig;

import cucumber.api.java.en.Then;
import cucumber.runtime.java.spring.beans.Belly;
import cucumber.runtime.java.spring.beans.BellyBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DefaultTestContextBootstrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ContextConfiguration("classpath:cucumber.xml")
@BootstrapWith(BellyStepdefs.DummyDefaultTestContextBootstrapper.class)
public class BellyStepdefs {

    @Autowired
    private Belly belly;

    @Autowired
    private BellyBean bellyBean;

    public BellyBean getBellyBean() {
        return bellyBean;
    }

    @Then("^I have belly$")
    public void I_have_belly() throws Throwable {
        assertNotNull(belly);
    }

    @Then("^I have belly bean$")
    public void I_have_belly_bean() throws Throwable {
        assertNotNull(bellyBean);
        assertTrue(DummyTestExecutionListener.calledBeforeTestClass.get() == 2);
        assertTrue(DummyTestExecutionListener.calledPrepareTestInstance.get() == 2);
        // The second call would happen after this test method/scenario finishes execution
        assertTrue(DummyTestExecutionListener.calledAfterTestClass.get() == 1);
    }

    public static class DummyDefaultTestContextBootstrapper extends DefaultTestContextBootstrapper {
        @Override
        protected Set<Class<? extends TestExecutionListener>> getDefaultTestExecutionListenerClasses() {
            Set<Class<? extends TestExecutionListener>> defaultTestExecutionListenerClasses = super.getDefaultTestExecutionListenerClasses();
            Set<Class<? extends TestExecutionListener>> results = new HashSet<Class<? extends TestExecutionListener>>(defaultTestExecutionListenerClasses);
            results.add(DummyTestExecutionListener.class);
            return results;
        }

        @Override
        protected List<String> getDefaultTestExecutionListenerClassNames() {
            List<String> defaultTestExecutionListenerClassNames = super.getDefaultTestExecutionListenerClassNames();
            List<String> results = new ArrayList<String>(defaultTestExecutionListenerClassNames);
            results.add(DummyTestExecutionListener.class.getName());
            return defaultTestExecutionListenerClassNames;
        }
    }

    public static class DummyTestExecutionListener extends AbstractTestExecutionListener {
        static AtomicInteger calledBeforeTestClass = new AtomicInteger();
        static AtomicInteger calledPrepareTestInstance = new AtomicInteger();
        static AtomicInteger calledAfterTestClass = new AtomicInteger();

        @Override
        public void beforeTestClass(TestContext testContext) throws Exception {
            calledBeforeTestClass.incrementAndGet();
            super.beforeTestClass(testContext);
        }

        @Override
        public void prepareTestInstance(TestContext testContext) throws Exception {
            calledPrepareTestInstance.incrementAndGet();
            super.prepareTestInstance(testContext);
        }

        @Override
        public void afterTestClass(TestContext testContext) throws Exception {
            calledAfterTestClass.incrementAndGet();
            super.afterTestClass(testContext);
        }
    }
}