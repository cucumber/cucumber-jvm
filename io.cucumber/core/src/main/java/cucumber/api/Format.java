package cucumber.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * This annotation can be specified on step definition method parameters to give Cucumber a hint
 * about how to transform a String into an object such as a Date or a Calendar. For example, if you have the following Gherkin step with
 * a <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> date:
 * </p>
 * <pre>
 * Given the date is 2012-03-01T06:54:12
 * </pre>
 * <p>
 * Then the following Java Step Definition would convert that into a Date:
 * </p>
 * <pre>
 * &#064;Given("^the date is (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})$")
 * public void the_date_is(@Format("yyyy-MM-dd'T'HH:mm:ss") Date date) {
 *     this.date = date;
 * }
 * </pre>
 * <p>
 * Or a Calendar:
 * </p>
 * <pre>
 * &#064;Given("^the date is (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})$")
 * public void the_date_is(@Format("yyyy-MM-dd'T'HH:mm:ss") Calendar cal) {
 *     this.cal = cal;
 * }
 * </pre>
 * <p>
 * This annotation also works for data tables that are transformed to a list of beans with Date or Calendar fields.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface Format {
    String value();
}
