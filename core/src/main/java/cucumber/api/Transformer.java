package cucumber.api;

import cucumber.runtime.xstream.converters.SingleValueConverter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Allows transformation of a step definition argument to a custom type, giving you full control
 * over how that type is instantiatd. Consider the following Gherkin step:
 * <p/>
 * <pre>Given I did my laundry 3 days ago</pre>
 * <p/>
 * Now, let's assume we want Cucumber to transform the substring "3 days ago" into an instance of our custom
 * <code>HumanTime</code> class:
 * <p/>
 * <pre>
 *     &#064;Given("I did my laundry (.*)")
 *     public void iDidMyLaundry(HumanTime t) {
 *     }
 * </pre>
 * <p/>
 * If the <code>HumanTime</code> class has a constructor with a single <code>String</code> argument, then
 * no explicit transformation is needed. If that's not the case you can annotate the class with your own converter:
 * <p/>
 * <pre>
 *     &#064;XStreamConverter(HumanTimeConverter.class)
 *     public class HumanTime {
 *     }
 * </pre>
 * And then a <code>HumanTimeConverter</code> class:
 * <p/>
 * <pre>{@code
 *     public static class HumanTimeConverter extends Transformer<HumanTime> {
 *         &#064;Override
 *         public HumanTime transform(String value) {
 *             // Parse the value here, and create a new instance.
 *             return new HumanTime(...);
 *         }
 *     }
 * }</pre>
 *
 * @param <T>
 */
public abstract class Transformer<T> implements SingleValueConverter {
    private final Type type;

    public Transformer() {
        ParameterizedType ptype = (ParameterizedType) getClass().getGenericSuperclass();
        this.type = ptype.getActualTypeArguments()[0];
    }

    @Override
    public String toString(Object o) {
        return o.toString();
    }

    @Override
    public Object fromString(String s) {
        return transform(s);
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(this.type);
    }

    public abstract T transform(String value);
}
