package cucumber.examples.spring.txn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.cucumber.examples.spring.txn.CucumberContextConfiguration;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CucumberContextConfiguration.class)
class ApplicationTest {

    @Test
    void contextLoads() {

    }
}
