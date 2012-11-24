package cucumber.examples.spring.txn;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.server.setup.MockMvcBuilders.xmlConfigSetup;

public class SeeMessagesSteps {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private User user;
    private Message message;

    private final String contextLocation = "classpath:cucumber.xml";
    private final String warRootDir = "/src/main/webapp";
    private ResultActions resultActions;

    @Given("^there is a User$")
    public void there_is_a_User() throws Throwable {
        user = userRepository.save(new User("John Doe"));
    }

    @Given("^the User has posted the message \"([^\"]*)\"$")
    public void the_User_has_posted_the_message(String content) throws Throwable {
        message = messageRepository.save(new Message(user, content));
    }

    @When("^I visit the page for the User$")
    public void I_visit_the_page_for_the_User() throws Throwable {
        resultActions = getMockMvc()
                .perform(get("/users/" + user.getId()))
                .andExpect(status().isOk());
    }

    @Then("^I should see \"([^\"]*)\"$")
    public void I_should_see(String content) throws Throwable {
        resultActions.andExpect(content().string(containsString(content)));
    }

    private MockMvc getMockMvc() {
        return xmlConfigSetup(contextLocation)
                .configureWebAppRootDir(warRootDir, false).build();
    }

}
