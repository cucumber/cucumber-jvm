package cucumber.annotation;

import java.lang.annotation.*;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Advice {
    /**
     * @return the pattern that is used to match a step with this advice
     */
    public String value();

    /**
     * @return the index of the group of the underlying step (within the
     *         pattern) whose step definition must be annotated with one
     *         of the Pointcut annotations returned by pointcuts().
     */
    public int stepGroup();

    /**
     * @return the pointcut annotations (i.e. they have to be annotated with
     * <code>Pointcut</code>) that this advice may advise.
     */
    public Class<? extends Annotation>[] pointcuts();

    /**
     * @return max amount of time this is allowed to run for. 0 (default) means no restriction.
     */
    public int timeout() default 0;
}
