package cucumber.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cucumber.runtime.java.CucumberAnnotation;

/**
 * 
 * Annotation to mark methods that can transform Strings arguments of steps to a
 * higher level, domain specific object.
 * 
 * @see cucumber.runtime.transformers.Transformable<T>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@CucumberAnnotation("en")
public @interface Transform {
}