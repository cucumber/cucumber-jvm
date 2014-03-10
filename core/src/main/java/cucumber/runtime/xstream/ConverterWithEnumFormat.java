package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.converters.ConversionException;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

class ConverterWithEnumFormat<T extends Enum> extends ConverterWithFormat<T> {

    private final List<Format> formats = new ArrayList<Format>();
    private final Locale locale;
    private final Class<? extends Enum> typeClass;

    ConverterWithEnumFormat(Locale locale, Class<? extends Enum> enumClass) {
        super(new Class[]{enumClass});
        this.locale = locale;
        this.typeClass = enumClass;
        formats.add(new OriginalFormat());
        formats.add(new LowercaseFormat());
        formats.add(new UppercaseFormat());
        formats.add(new CapitalizeFormat());
    }


    @Override
    public T transform(String string) {
        try {
            return super.transform(string);
        } catch (ConversionException e) {
            String allowed = asList(typeClass.getEnumConstants()).toString();
            throw new ConversionException(String.format("Couldn't convert %s to %s. Legal values are %s", string, typeClass.getName(), allowed));
        }
    }

    @Override
    public List<Format> getFormats() {
        return formats;
    }

    private class OriginalFormat extends AbstractEnumFormat {
        @Override
        protected String transformSource(String source) {
            return source;
        }
    }

    private class LowercaseFormat extends AbstractEnumFormat {
        @Override
        protected String transformSource(String source) {
            return source.toLowerCase(locale);
        }
    }

    private class UppercaseFormat extends AbstractEnumFormat {
        @Override
        protected String transformSource(String source) {
            return source.toUpperCase(locale);
        }
    }

    private class CapitalizeFormat extends AbstractEnumFormat {
        @Override
        protected String transformSource(String source) {
            String firstLetter = source.substring(0, 1);
            String restOfTheString = source.substring(1, source.length());
            return firstLetter.toUpperCase(locale) + restOfTheString;
        }
    }

    private abstract class AbstractEnumFormat extends Format {
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(String.valueOf(obj));
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return source == null ? null : Enum.valueOf(typeClass, transformSource(source));
        }

        protected abstract String transformSource(String source);

    }

}
