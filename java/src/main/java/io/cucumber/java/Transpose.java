package io.cucumber.java;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * This annotation can be specified on step definition method parameters to give Cucumber a hint
 * to transpose a DataTable.
 * <p>
 * For example, if you have the following Gherkin step with a table
 * <pre>
 * Given the user is
 *    | firstname	| Roberto	|
 *    | lastname	| Lo Giacco |
 *    | nationality	| Italian	|
 * </pre>
 * <p>
 * And a data table type to create a User
 *
 * <pre>
 * typeRegistry.defineDataTableType(new DataTableType(
 *    Author.class,
 *    new TableEntryTransformer&lt;User&gt;() {
 *    &#064;Override
 *    public Author transform(Map&lt;String, String&gt; entry) {
 *       return new User(
 *          entry.get("firstName"),
 *          entry.get("lastName"),
 *          entry.get("nationality"));
 *    }
 * }));
 *
 * </pre>
 * Then the following Java Step Definition would convert that into an User object:
 * <pre>
 * &#064;Given("^the user is$")
 * public void the_user_is(&#064;Transpose User user) {
 *     this.user = user;
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@API(status = API.Status.STABLE)
public @interface Transpose {
    boolean value() default true;
}
