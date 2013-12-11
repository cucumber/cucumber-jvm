package cucumber.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * This annotation can be specified on step definition method parameters to give Cucumber a hint
 * to transpose a DataTable into an object or list of objects. 
 * 
 * For example, if you have the following Gherkin step with a table
 * </p>
 * <pre>
 * Given the user is
 *    | firstname	| Roberto	|
 *    | lastname	| Lo Giacco |
 *    | nationality	| Italian	|
 * </pre>
 * <p>
 * Then the following Java Step Definition would convert that into an User object:
 * </p>
 * <pre>
 * &#064;Given("^the user is$")
 * public void the_user_is(@Transpose User user) {
 *     this.user = user;
 * }
 * </pre>
 * <p>
 * 
 * This annotation also works for data tables that are transformed to a list of beans.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Transpose {
    boolean value() default true;
}
