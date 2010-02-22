package cuke4duke.annotation;

import cuke4duke.internal.java.annotation.StepDef;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ZHTW {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface 假設 {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface 當 {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface 那麼 {
        public abstract String value();
    }

}