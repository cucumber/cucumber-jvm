package cucumber.runtime.java.hk2;

import org.glassfish.hk2.utilities.Binder;


import java.lang.annotation.*;

import static java.lang.Boolean.TYPE;

/**
 * Created by yorta01 on 9/30/2016.
 */
@Documented
@Inherited
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Hk2Binders {
    /**
     * Create a service locator and populate it with services defined in
     * "META-INF/hk2-locator/default" inhabitant files found in the classpath.
     *
     * @return true if the classpath should be scanned for inhabitant files.
     */
    boolean populate() default true;

    /**
     * A list of binders that should be loaded.
     *
     * @return a list of binders classes
     */
    Class<? extends Binder>[] binders() default {};
}
