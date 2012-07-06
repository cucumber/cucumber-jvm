package cucumber.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker interface for annotations which will be recognized as a pointcut.
 * Pointcut annotations document an annotated cucumber method being able to
 * to be called after some action specified by the pointcut handler.
 *
 * An application for this is testing an application with different timing
 * settings. The relevant actions themselves are implemented as stepdefs and
 * the different kind of Timing Mechanisms are implemented as pointcut handlers
 * which are stepdefs themselves but are able to call the other one dynamically.
 *
 * This is not just a nested stepdef, it is more like a pre-processing step
 * where the callee can control who is able to extend its functionality.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Pointcut {
}
