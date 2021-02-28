package io.cucumber.spring.threading;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

@CucumberContextConfiguration
@WebAppConfiguration
@ContextConfiguration("classpath:cucumber.xml")
public class ThreadingStepDefinitions {

    static final int concurrency = 5;
    static final ConcurrentHashMap<Thread, ThreadingStepDefinitions> map = new ConcurrentHashMap<>();

    private static final CountDownLatch latch = new CountDownLatch(2);

    @Given("I am a step definition")
    public void iAmAStepDefinition() {
        map.put(currentThread(), this);
    }

    @When("when executed in parallel")
    public void whenExecutedInParallel() throws Throwable {
        latch.await(1, TimeUnit.SECONDS);
    }

    @Then("I should not be shared between threads")
    public void iShouldNotBeSharedBetweenThreads() {
        for (Map.Entry<Thread, ThreadingStepDefinitions> entries : map.entrySet()) {
            if (entries.getKey().equals(currentThread())) {
                assertSame(entries.getValue(), this);
            } else {
                assertNotSame(entries.getValue(), this);
            }
        }
        assertEquals(concurrency, map.size());
    }

}
