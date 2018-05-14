package cucumber.examples.spring.txn;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SeeMessagesStepdefs {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MockMvc mockMvc;

    private User user;

    private ResultActions resultActions;

    @Given("there is a User")
    public void there_is_a_User() {
        user = userRepository.save(new User("John Doe"));
    }

    @Given("the User has posted the message {string}")
    public void the_User_has_posted_the_message(String content) {
        messageRepository.save(new Message(user, content));
    }

    @When("I visit the page for the User")
    public void I_visit_the_page_for_the_User() throws Exception {
        resultActions = mockMvc
                .perform(get("/users/" + user.getId()))
                .andExpect(status().isOk());
    }

    @Then("I should see {string}")
    public void I_should_see(String content) throws Exception {
        resultActions.andExpect(content().string(containsString(content)));
    }

}
