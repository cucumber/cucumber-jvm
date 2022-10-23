package io.cucumber.spring.cucontextconfig;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.*;

@MyTestAnnotation
public class WithMetaAnnotation {
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited

@CucumberContextConfiguration // Required for Cucumber + Spring
@ComponentScan("some.test.stuff")
@ContextConfiguration(classes = SomeGeneralPurposeTestContext.class)
@TestPropertySource(properties = "testprop=value_for_testing")
@ActiveProfiles("test")
@interface MyTestAnnotation {

}

class SomeGeneralPurposeTestContext {
}
