package cucumber.runtime.converters;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.SingleValueConverterWrapper;
import com.thoughtworks.xstream.core.DefaultConverterLookup;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConverterLookups {
    private final Map<Locale,ConverterLookup> lookups = new HashMap<Locale, ConverterLookup>();
    
    public ConverterLookup forLocale(Locale locale) {
        ConverterLookup lookup = lookups.get(locale);
        if(lookup == null) {
            lookup = newLookup(locale);
            lookups.put(locale, lookup);
        }
        return lookup;
    }

    private ConverterLookup newLookup(Locale locale) {
        DefaultConverterLookup lookup = new DefaultConverterLookup();
        
        // Calling XStream's ctor is currently the only way to have all the default converters registered :-/
        new XStream(null, null, Thread.currentThread().getContextClassLoader(), null, lookup, lookup);

        // Override what XStream does with our own Locale-aware converters.
        register(lookup, new BigDecimalConverter(locale));
        register(lookup, new BigIntegerConverter(locale));
        register(lookup, new ByteConverter(locale));
        register(lookup, new DateConverter(locale)); // TODO: pass in format!!
        register(lookup, new DoubleConverter(locale));
        register(lookup, new FloatConverter(locale));
        register(lookup, new IntegerConverter(locale));
        register(lookup, new LongConverter(locale));

        return lookup;
    }

    private void register(DefaultConverterLookup lookup, SingleValueConverter conve) {
        lookup.registerConverter(new SingleValueConverterWrapper(conve), 10000);
    }
}
