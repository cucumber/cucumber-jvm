package cucumber.examples.spring.txn;

import cucumber.api.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;

@WebAppConfiguration
@ContextConfiguration("classpath:cucumber.xml")
public class TransactionCheckHooks {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Before
    public void verifyEmptyDatabase() {
        assertEquals(0, userRepository.count());
        assertEquals(0, messageRepository.count());
    }
}
