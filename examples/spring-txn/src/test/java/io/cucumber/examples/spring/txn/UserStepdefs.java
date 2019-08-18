package io.cucumber.examples.spring.txn;

import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserStepdefs {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private User user;

    public void thereIsAuser() {
        user = new User();
        user.setUsername("testuser");
        userRepository.save(user);
    }

    @Given("a User has posted the following messages:")
    public void a_User_has_posted_the_following_messages(List<Message> messages) {
        thereIsAuser();
        for (Message m : messages) {
            m.setAuthor(user);
            messageRepository.save(m);
        }
        assertTrue(
            TransactionSynchronizationManager.isActualTransactionActive(),
            "No transaction is active"
        );
    }
}
