package cucumber.api;

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
 * </p>
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
 *    new TableEntryTransformer<User>() {
 *    @Override
 *    public Author transform(Map<String, String> entry) {
 *       return new User(
 *          entry.get("firstName"),
 *          entry.get("lastName"),
 *          entry.get("nationality"));
 *    }
 * }));
 *
 * </pre>
 * Then the following Java Step Definition would convert that into an User object:
 * </p>
 * <pre>
 * &#064;Given("^the user is$")
 * public void the_user_is(@Transpose User user) {
 *     this.user = user;
 * }
 * </pre>
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Transpose {
    boolean value() default true;
}
