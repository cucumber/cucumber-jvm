package cucumber.annotation;

import cucumber.runtime.java.CucumberAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class FI {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fi")
    // TODO: REMOVE
    public static @interface Oletetaan {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fi")
    // TODO: REMOVE
    public static @interface Kun {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fi")
    // TODO: REMOVE
    public static @interface Niin {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fi")
    // TODO: REMOVE
    public static @interface Ja {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fi")
    // TODO: REMOVE
    public static @interface Mutta {
        public abstract String value();
    }
}

