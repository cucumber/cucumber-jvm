package cucumber.annotation;

import cucumber.runtime.java.CucumberAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class ID {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("id")
    // TODO: REMOVE
    public static @interface Dengan {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("id")
    // TODO: REMOVE
    public static @interface Ketika {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("id")
    // TODO: REMOVE
    public static @interface Maka {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("id")
    // TODO: REMOVE
    public static @interface Dan {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("id")
    // TODO: REMOVE
    public static @interface Tapi {
        public abstract String value();
    }
}

