package cucumber.annotation;

import cucumber.runtime.java.CucumberAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class SR_CYRL {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("sr-Cyrl") // TODO: REMOVE
    public static @interface Задато {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("sr-Cyrl") // TODO: REMOVE
    public static @interface Задате {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("sr-Cyrl") // TODO: REMOVE
    public static @interface Задати {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("sr-Cyrl") // TODO: REMOVE
    public static @interface Када {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("sr-Cyrl") // TODO: REMOVE
    public static @interface Кад {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("sr-Cyrl") // TODO: REMOVE
    public static @interface Онда {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("sr-Cyrl") // TODO: REMOVE
    public static @interface И {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("sr-Cyrl") // TODO: REMOVE
    public static @interface Али {
        public abstract String value();
    }
}

