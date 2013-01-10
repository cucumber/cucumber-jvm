package cucumber.api;

import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.runtime.ParameterInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Locale;

/**
 * <p>
 * Allows transformation of a step definition argument to a custom type, giving you full control
 * over how that type is instantiated.
 * </p>
 * <p>
 * Consider the following Gherkin step:
 * </p>
 * <pre>Given today's date is "10/03/1985"</pre>
 * <p>
 * As an example, let's assume we want Cucumber to transform the substring "10/03/1985" into an instance of
 * <code>org.joda.time.LocalDate</code> class:
 * </p>
 * <pre>
 *     &#064;Given("today's date is \"(.*)\"")
 *     public void todays_date_is(LocalDate d) {
 *     }
 * </pre>
 * <p>
 * If the parameter's class has a constructor with a single <code>String</code> or <code>Object</code> argument, then
 * Cucumber will instantiate it without any further ado. However, in this case that might not give you what you
 * want. Depending on your Locale, the date may be Oct 3 or March 10!
 *
 * </p>
 * <p>
 *     This is when you can use a custom transformer. You'll also have to do that if your parameter class doesn't
 *     have a constructor with a single <code>String</code> or <code>Object</code> argument. For the JODA Time
 *     example:
 * </p>
 *
 * <pre>
 *     &#064;Given("today's date is \"(.*)\"")
 *     public void todays_date_is(&#064;Transform(JodaTimeConverter.class) LocalDate d) {
 *     }
 * </pre>
 * <p>
 * And then a <code>JodaTimeConverter</code> class:
 * </p>
 * <pre>{@code
 *     public static class JodaTimeConverter extends Transformer<LocalDate> {
 *         private static DateTimeFormatter FORMATTER = DateTimeFormat.forStyle("S-");
 *
 *         &#064;Override
 *         public LocalDate transform(String value) {
 *             return FORMATTER.withLocale(getLocale()).parseLocalDate(value);
 *         }
 *     }
 * }</pre>
 * <p>
 * An alternative to annotating parameters with {@link Transform} is to annotate your class with
 * {@link cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter}:
 * </p>
 * <pre>
 *     &#064;XStreamConverter(MyConverter.class)
 *     public class MyClass {
 *     }
 * </pre>
 * <p>
 * This will also enable a {@link DataTable} to be transformed to
 * a <code>List&lt;MyClass;&gt;</code>
 * </p>
 *
 * @param <T> the type to be instantiated
 * @see Transform
 */
public abstract class Transformer<T> implements SingleValueConverter {
    private final Type type;
    private Locale locale;

    public Transformer() {
        ParameterizedType ptype = (ParameterizedType) getClass().getGenericSuperclass();
        this.type = ptype.getActualTypeArguments()[0];
    }

    @Override
    public String toString(Object o) {
        return o.toString();
    }

    @Override
    public final Object fromString(String s) {
        return transform(s);
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(this.type);
    }

    public abstract T transform(String value);

    public void setParameterInfoAndLocale(ParameterInfo parameterInfo, Locale locale) {
        this.locale = locale;
    }

    /**
     * @return the current locale
     */
    protected Locale getLocale() {
        return locale;
    }
}
