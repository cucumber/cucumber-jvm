package io.cucumber.examples.spring.txn;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@io.cucumber.spring.CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
public class CucumberContextConfiguration  {

}
