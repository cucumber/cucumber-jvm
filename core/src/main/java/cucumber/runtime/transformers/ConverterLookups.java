package cucumber.runtime.transformers;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConverterLookup;
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
        
        new XStream(null, null, Thread.currentThread().getContextClassLoader(), null, lookup, lookup);
        lookup.registerConverter(new SingleValueConverterWrapper(new FloatTransformer(locale)), 10000);
        
        return lookup;
    }
}
