package io.cucumber.spring.cucumbercontextconfigannotation;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration("classpath:cucumber.xml")
public abstract class AbstractWithComponentAnnotation {
}
