package io.cucumber.spring.cucumbercontextconfigannotation;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.*;

public class WithInheritedAnnotation extends ParentClass {
}

@InheritableCumberContextConfiguration
class ParentClass {
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@CucumberContextConfiguration
@ContextConfiguration("classpath:cucumber.xml")
@Inherited
@interface InheritableCumberContextConfiguration {
}
