package cucumber.runtime.java.hooks;

import cucumber.api.java.*;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.StepDefAnnotation;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@StepDefAnnotation
public class HooksStepDefs {

    private static HttpServerStub httpServer = new HttpServerStub();
    private HttpServerStub.Request<String> simpleRequest = new HttpServerStub.Request<String>("data to save");
    private int responseCode = 0;
    private String data;
    private String dataToRestore;
    private static List<String> hooksOrder = new ArrayList<String>();
    static {
        // hooks order for two feature files, each with 2 scenarios,
        // the second feature has tags for custom feature hooks - BeforeFeature(@...) and AfterFeature(@...)

        // global hook before tests
        hooksOrder.add("@BeforeAll");

        // first feature expected hooks in order
        hooksOrder.add("@Before");
        hooksOrder.add("@After");
        hooksOrder.add("@Before");
        hooksOrder.add("@After");

        // second feature expected hooks in order
        hooksOrder.add("@Before");
        hooksOrder.add("@After");
        hooksOrder.add("@Before");
        hooksOrder.add("@After");

        // global hook after tests
        hooksOrder.add("@AfterAll");
    }

    @BeforeAll
    public void startServer() {
        ensureHooksOrder("@BeforeAll");
        httpServer = new HttpServerStub();
        httpServer.start();
        assertThat("The server should be started", httpServer.isStarted(), is(true));
    }

    @AfterAll
    public void stopServer() {
        ensureHooksOrder("@AfterAll");
        assertThat("The server should be started",httpServer.isStarted(), is(true));
        httpServer.stop();
        assertThat("The server should be stopped",httpServer.isStarted(), is(false));
    }

    @Before
    public void beforeScenario() {
        ensureHooksOrder("@Before");
    }

    @After
    public void afterScenario() {
        ensureHooksOrder("@After");
    }

    @Given("^that we have a http server up and running and no data on it$")
    public void that_we_have_a_http_server_up_and_running_and_no_data_on_it() throws Throwable {
        assertThat(httpServer.receive(), is(nullValue()));
    }

    @Given("^that we have a http server up and running$")
    public void a_up_and_running_http_server() throws Throwable {
        assertThat("The server should be started",httpServer.isStarted(), is(true));
    }

    @When("^I send a request to save some data$")
    public void I_send_a_request_to_save_some_data() {
        responseCode = httpServer.send(simpleRequest);
    }

    @When("^I send a request to get the existing data on http server$")
    public void i_send_a_request_to_get_the_existing_data_on_http_server() throws Throwable {
        data = httpServer.receive();
    }

    @Then("^I expect a success response code of (\\d+)$")
    public void I_expect_a_success_response_code_of(int code) {
        assertThat("Unexpected response code. expected: " + code + " actual: " + responseCode, responseCode, is(code));
    }

    @Then("^I expect to get back some data$")
    public void I_expect_to_get_back_some_data() throws Throwable {
        assertThat(data, is(simpleRequest.getData()));
    }

    private void ensureHooksOrder(String currentHook) {
        String expected = hooksOrder.remove(0);
       assertThat(currentHook, is(expected));
    }
}
