package cuke4duke.annotation;

import cuke4duke.internal.java.annotation.StepDef;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class HU {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface Ha {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface Majd {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface Akkor {
        public abstract String value();
    }

}