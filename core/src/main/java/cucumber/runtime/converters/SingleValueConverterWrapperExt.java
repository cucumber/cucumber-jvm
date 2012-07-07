package cucumber.runtime.converters;

import cucumber.runtime.xstream.converters.SingleValueConverter;
import cucumber.runtime.xstream.converters.SingleValueConverterWrapper;

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
