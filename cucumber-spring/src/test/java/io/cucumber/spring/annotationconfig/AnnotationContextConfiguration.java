package io.cucumber.spring.annotationconfig;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:cucumber.xml")
@CucumberContextConfiguration
@SuppressWarnings("DesignForExtension")
public class AnnotationContextConfiguration {

}
