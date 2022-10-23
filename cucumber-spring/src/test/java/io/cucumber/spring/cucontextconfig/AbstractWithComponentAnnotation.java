package io.cucumber.spring.cucontextconfig;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration("classpath:cucumber.xml")
public abstract class AbstractWithComponentAnnotation {
}
