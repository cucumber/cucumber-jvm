package cucumber.runtime;

import cucumber.api.Transform;
import cucumber.api.Transformer;
import cucumber.runtime.converters.LocalizedXStreams;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class ParameterTypeTest {

    private static final Locale LOCALE = Locale.US;
    private static final LocalizedXStreams.LocalizedXStream X = new LocalizedXStreams(Thread.currentThread().getContextClassLoader()).get(LOCALE);

    public void withInt(int i) {
    }

    @Test
    public void converts_with_built_in_converter() throws NoSuchMethodException {
        ParameterType pt = ParameterType.fromMethod(getClass().getMethod("withInt", Integer.TYPE)).get(0);
        assertEquals(23, pt.convert("23", X, LOCALE));
    }

    public void withCustomTransform(@Transform(UppercasedTransformer.class) Uppercased uppercased) {
    }

    public static class Uppercased {
        public String value;

        public Uppercased(String value) {

            this.value = value;
        }
    }

    public static class UppercasedTransformer extends Transformer<Uppercased> {
        @Override
        public Uppercased transform(String value) {
            return new Uppercased(value.toUpperCase());
        }
    }

    @Test
    public void converts_with_custom_transform() throws NoSuchMethodException {
        ParameterType pt = ParameterType.fromMethod(getClass().getMethod("withCustomTransform", Uppercased.class)).get(0);
        assertEquals("HELLO", ((Uppercased) pt.convert("hello", X, LOCALE)).value);
    }

    public static class FortyTwoTransformer extends Transformer<Integer> {
        @Override
        public Integer transform(String value) {
            return 42;
        }
    }

    public void intWithCustomTransform(@Transform(FortyTwoTransformer.class) int n) {
    }

    @Test
    public void converts_int_with_custom_transform() throws NoSuchMethodException {
        ParameterType pt = ParameterType.fromMethod(getClass().getMethod("intWithCustomTransform", Integer.TYPE)).get(0);
        assertEquals(42, pt.convert("hello", X, LOCALE));
    }
}
