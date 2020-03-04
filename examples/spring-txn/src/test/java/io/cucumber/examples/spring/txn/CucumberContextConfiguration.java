package io.cucumber.examples.spring.txn;

import io.cucumber.spring.CucumberSpringTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberSpringTest
@SpringBootTest
@AutoConfigureMockMvc
public class CucumberContextConfiguration  {

}
