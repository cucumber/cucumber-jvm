package io.cucumber.spring.cucumbercontextconfigannotation;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

@MyTestAnnotation
public class WithMetaAnnotation {
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CucumberContextConfiguration
@ContextConfiguration("classpath:cucumber.xml")
@interface MyTestAnnotation {
}
