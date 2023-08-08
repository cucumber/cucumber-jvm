package io.cucumber.spring.cucumbercontextconfigannotation;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
