package cuke4duke;

import cuke4duke.internal.java.annotation.StepDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@StepDef
public @interface Then {
    String value();
}
