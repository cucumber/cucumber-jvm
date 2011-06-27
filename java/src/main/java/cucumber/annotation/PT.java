package cucumber.annotation;

import cucumber.runtime.java.CucumberAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class PT {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("pt")
    // TODO: REMOVE
    public static @interface Dado {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("pt")
    // TODO: REMOVE
    public static @interface Quando {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("pt")
    // TODO: REMOVE
    public static @interface Então {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("pt")
    // TODO: REMOVE
    public static @interface Entao {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("pt")
    // TODO: REMOVE
    public static @interface E {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("pt")
    // TODO: REMOVE
    public static @interface Mas {
        public abstract String value();
    }
}

