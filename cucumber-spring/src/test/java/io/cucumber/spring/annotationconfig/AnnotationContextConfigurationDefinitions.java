package io.cucumber.spring.annotationconfig;

import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AnnotationContextConfigurationDefinitions {

    @Autowired
    private ApplicationContext context;

    @Then("cucumber picks up configuration class without step defs")
    public void pickUpContext() {
        assertNotNull(context);
    }

}
