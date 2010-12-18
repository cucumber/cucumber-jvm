package cucumber.annotation;

import cucumber.runtime.java.CucumberAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class EN_SCOUSE {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("en-Scouse") // TODO: REMOVE
    public static @interface Givun {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("en-Scouse") // TODO: REMOVE
    public static @interface Youseknowwhenyousegot {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("en-Scouse") // TODO: REMOVE
    public static @interface Wun {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("en-Scouse") // TODO: REMOVE
    public static @interface Youseknowlikewhen {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("en-Scouse") // TODO: REMOVE
    public static @interface Dun {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("en-Scouse") // TODO: REMOVE
    public static @interface Denyousegotta {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("en-Scouse") // TODO: REMOVE
    public static @interface An {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("en-Scouse") // TODO: REMOVE
    public static @interface Buh {
        public abstract String value();
    }
}

