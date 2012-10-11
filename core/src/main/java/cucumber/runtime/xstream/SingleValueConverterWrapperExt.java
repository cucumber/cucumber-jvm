package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverterWrapper;

/**
 * Subclass that exposes the wrapped converter
 */
class SingleValueConverterWrapperExt extends SingleValueConverterWrapper {
    private final SingleValueConverter converter;

    public SingleValueConverterWrapperExt(SingleValueConverter converter) {
        super(converter);
        this.converter = converter;
    }

    public SingleValueConverter getConverter() {
        return converter;
    }
}
