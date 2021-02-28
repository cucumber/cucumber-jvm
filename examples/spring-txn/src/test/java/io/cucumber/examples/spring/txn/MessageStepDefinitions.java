package io.cucumber.examples.spring.txn;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class MessageStepDefinitions {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserStepDefinitions userStepDefinitions;

    private List<Message> results;

    @Given("the user has posted the message {string}")
    public void the_user_has_posted_the_message(String content) {
        User user = userStepDefinitions.getCurrentUser();
        messageRepository.save(new Message(user, content));
    }

    @Given("a User has posted the following messages:")
    public void a_user_has_posted_the_following_messages(List<Message> messages) {
        User user = userStepDefinitions.getCurrentUser();
        for (Message m : messages) {
            m.setAuthor(user);
            messageRepository.save(m);
        }
    }

    @When("I search for {string}")
    public void i_search_for(String query) {
        this.results = messageRepository.findByContentContaining(query);
    }

    @Then("the results content should be:")
    public void the_result_should_be(List<String> contents) {
        User user = userStepDefinitions.getCurrentUser();
        assertAll(() -> {
            assertThat(results)
                    .extracting(Message::getContent)
                    .isEqualTo(contents);
            assertThat(results)
                    .extracting(Message::getAuthor)
                    .extracting(User::getId)
                    .allMatch(user.getId()::equals);
        });
    }

}
