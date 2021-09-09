package io.cucumber.spring.annotationconfig;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
@ContextConfiguration("classpath:cucumber.xml")
@CucumberContextConfiguration
public class AnnotationContextConfiguration {

}
