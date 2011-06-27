package cucumber.annotation;

import cucumber.runtime.java.CucumberAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class FR {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fr")
    // TODO: REMOVE
    public static @interface Soit {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fr")
    // TODO: REMOVE
    public static @interface Etantdonné {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fr")
    // TODO: REMOVE
    public static @interface Quand {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fr")
    // TODO: REMOVE
    public static @interface Lorsque {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fr")
    // TODO: REMOVE
    public static @interface Lorsqu {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fr")
    // TODO: REMOVE
    public static @interface Alors {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fr")
    // TODO: REMOVE
    public static @interface Et {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("fr")
    // TODO: REMOVE
    public static @interface Mais {
        public abstract String value();
    }
}

