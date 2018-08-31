package io.cucumber.spring.threading;

import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@WebAppConfiguration
@ContextConfiguration("classpath:cucumber.xml")
public class ThreadingStepDefs {

    private static final ConcurrentHashMap<Thread, ThreadingStepDefs> map = new ConcurrentHashMap<Thread, ThreadingStepDefs>();

    private static final CountDownLatch latch = new CountDownLatch(2);

    @Given("I am a step definition")
    public void iAmAStepDefinition() throws Throwable {
        map.put(currentThread(), this);
    }

    @When("when executed in parallel")
    public void whenExecutedInParallel() throws Throwable {
        latch.await(1, TimeUnit.SECONDS);
    }

    @Then("I should not be shared between threads")
    public void iShouldNotBeSharedBetweenThreads() throws Throwable {
        for (Map.Entry<Thread, ThreadingStepDefs> entries : map.entrySet()) {
            if (entries.getKey().equals(currentThread())) {
                assertSame(entries.getValue(), this);
            } else {
                assertNotSame(entries.getValue(), this);
            }
        }
        assertEquals(2, map.size());
    }
}
