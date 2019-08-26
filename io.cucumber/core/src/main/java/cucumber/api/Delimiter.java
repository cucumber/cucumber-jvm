package cucumber.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * This annotation can be specified on step definition method parameters to give Cucumber a hint
 * about how to transform a String to a list of objects. For example, if you have the following Gherkin step:
 * </p>
 * <pre>
 * Given the users adam, bob, john
 * </pre>
 * <p>
 * Then the following Java Step Definition would convert that into a List:
 * </p>
 * <pre>
 * &#064;Given("^the users ([a-z](?:, [a-z]+))$")
 * public void the_users(&#064;Delimiter(", ") List&lt;String&gt; users) {
 *     this.users = users;
 * }
 * </pre>
 * <p>
 * This annotation also works with regular expression patterns. Step definition method parameters of type
 * {@link java.util.List} without the {@link Delimiter} annotation will default to the pattern {@code ",\\s?"}.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface Delimiter {
    String value();
}
