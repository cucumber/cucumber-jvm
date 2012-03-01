package cucumber;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be specified on step definition method parameters to give Cucumber a hint
 * about how to transform a string to a Date. For example, if you have the following Gherkin step:
 *
 * <pre>
 * </pre>
 *
 * Then the following Java Step Definition would convert that into an <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> Date:
 *
 * <pre>
 * &#064;Given("^the date is (.+)$")
 * public void the_date_is(@DateFormat("yyyy/MM/dd") Date date) {
 *     this.date = date;
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface DateFormat {
    String value();
}
