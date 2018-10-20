package cucumber.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CucumberConfiguration {
  Type type() default Type.YAML;

  String path();

  public enum Type {
    YAML, JSON, PROPERTIES, ANNOTATION;
  }
}
