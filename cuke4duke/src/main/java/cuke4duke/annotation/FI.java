package cuke4duke.annotation;

import cuke4duke.internal.java.annotation.StepDef;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class FI {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface Oletetaan {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface Kun {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface Niin {
        public abstract String value();
    }

}