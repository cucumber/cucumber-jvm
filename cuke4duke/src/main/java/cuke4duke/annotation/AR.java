package cuke4duke.annotation;

import cuke4duke.internal.java.annotation.StepDef;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class AR {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface بفرض {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface متى {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface عندما {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface اذاً {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @StepDef
    public static @interface ثم {
        public abstract String value();
    }

}