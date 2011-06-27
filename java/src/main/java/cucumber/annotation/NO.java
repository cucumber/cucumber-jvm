package cucumber.annotation;

import cucumber.runtime.java.CucumberAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class NO {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("no")
    // TODO: REMOVE
    public static @interface Gitt {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("no")
    // TODO: REMOVE
    public static @interface Når {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("no")
    // TODO: REMOVE
    public static @interface Så {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("no")
    // TODO: REMOVE
    public static @interface Og {
        public abstract String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @CucumberAnnotation("no")
    // TODO: REMOVE
    public static @interface Men {
        public abstract String value();
    }
}

