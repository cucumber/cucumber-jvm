package cucumber.runtime;

import cucumber.api.Delimiter;
import cucumber.api.Format;
import cucumber.api.Transform;
import cucumber.api.Transformer;
import cucumber.runtime.xstream.LocalizedXStreams;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class ParameterInfoTest {

    private static final Locale LOCALE = Locale.US;
    private static final LocalizedXStreams.LocalizedXStream X = new LocalizedXStreams(Thread.currentThread().getContextClassLoader()).get(LOCALE);

    public void withInt(int i) {
    }

    @Test
    public void converts_with_built_in_converter() throws NoSuchMethodException {
        ParameterInfo pt = ParameterInfo.fromMethod(getClass().getMethod("withInt", Integer.TYPE)).get(0);
        assertEquals(23, pt.convert("23", X, LOCALE));
    }

    public void withJodaTime(@Transform(JodaTransformer.class) LocalDate date) {
    }

    public static class JodaTransformer extends Transformer<LocalDate> {
        private static DateTimeFormatter FORMATTER = DateTimeFormat.forStyle("S-");

        @Override
        public LocalDate transform(String value) {
            return FORMATTER.withLocale(getLocale()).parseLocalDate(value);
        }
    }

    @Test
    public void converts_with_custom_joda_time_transform_and_format() throws NoSuchMethodException {
        ParameterInfo parameterInfo = ParameterInfo.fromMethod(getClass().getMethod("withJodaTime", LocalDate.class)).get(0);
        LocalDate aslaksBirthday = new LocalDate(1971,2,28);
        assertEquals(aslaksBirthday, parameterInfo.convert("28/02/1971", X, Locale.FRANCE));
        assertEquals(aslaksBirthday, parameterInfo.convert("02/28/1971", X, Locale.US));
    }

    public void withJodaTimeAndFormat(@Transform(JodaTransformer.class) @Format("S-") LocalDate date) {
    }

    public static class JodaFormatTransformer extends Transformer<LocalDate> {
        @Override
        public LocalDate transform(String value) {
            String format = getParameterInfo().getFormat();
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forStyle(format);
            dateTimeFormatter = dateTimeFormatter.withLocale(getLocale());
            return dateTimeFormatter.parseLocalDate(value);
        }
    }

    @Test
    public void converts_with_custom_joda_time_transform() throws NoSuchMethodException {
        ParameterInfo parameterInfo = ParameterInfo.fromMethod(getClass().getMethod("withJodaTimeAndFormat", LocalDate.class)).get(0);
        LocalDate aslaksBirthday = new LocalDate(1971,2,28);
        assertEquals(aslaksBirthday, parameterInfo.convert("28/02/1971", X, Locale.FRANCE));
        assertEquals(aslaksBirthday, parameterInfo.convert("02/28/1971", X, Locale.US));
    }

    public void withJodaTimeWithoutTransform(LocalDate date) {
    }

    @Test
    public void converts_to_joda_time_using_object_ctor_and_default_locale() throws NoSuchMethodException {
        ParameterInfo parameterInfo = ParameterInfo.fromMethod(getClass().getMethod("withJodaTimeWithoutTransform", LocalDate.class)).get(0);
        LocalDate localDate = new LocalDate("1971");
        assertEquals(localDate, parameterInfo.convert("1971", X, Locale.FRANCE));
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
        ParameterInfo pt = ParameterInfo.fromMethod(getClass().getMethod("intWithCustomTransform", Integer.TYPE)).get(0);
        assertEquals(42, pt.convert("hello", X, LOCALE));
    }

    public void listWithNoDelimiter(List<String> list) {
    }

    @Test
    public void converts_list_with_default_delimiter() throws NoSuchMethodException {
        ParameterInfo pt = ParameterInfo.fromMethod(getClass().getMethod("listWithNoDelimiter", List.class)).get(0);
        assertEquals(Arrays.asList("hello", "world"), pt.convert("hello, world", X, LOCALE));
        assertEquals(Arrays.asList("hello", "world"), pt.convert("hello,world", X, LOCALE));
    }

    public void listWithCustomDelimiter(@Delimiter("\\|") List<String> list) {
    }

    @Test
    public void converts_list_with_custom_delimiter() throws NoSuchMethodException {
        ParameterInfo pt = ParameterInfo.fromMethod(getClass().getMethod("listWithCustomDelimiter", List.class)).get(0);
        assertEquals(Arrays.asList("hello", "world"), pt.convert("hello|world", X, LOCALE));
    }

    public void listWithNoTypeArgument(List list) {
    }

    @Test
    public void converts_list_with_no_type_argument() throws NoSuchMethodException {
        ParameterInfo pt = ParameterInfo.fromMethod(getClass().getMethod("listWithNoTypeArgument", List.class)).get(0);
        assertEquals(Arrays.asList("hello", "world"), pt.convert("hello, world", X, LOCALE));
    }
}
