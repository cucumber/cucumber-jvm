package io.cucumber.core.options;

import io.cucumber.core.order.DefaultPickleOrderFactory;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.PickleOrderFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PickleOrderParser {

    private static final Pattern NAME_VS_ARGUMENT_PATTERN = Pattern.compile("([a-z_]+)(?::(.+))?");

    static PickleOrder parse(String argument) {
        Matcher matcher = NAME_VS_ARGUMENT_PATTERN.matcher(argument);
        if (matcher.matches()) {
            return parse(matcher.group(1), matcher.group(2));
        }
        return parse(argument, null);
    }

    static PickleOrder parse(String name, String argument) {
        return getFactory().create(name, argument);
    }

    static PickleOrderFactory getFactory() {
        //TODO: get instance from SPI, missing configuration parameter for the factory
        return new DefaultPickleOrderFactory();
    }


}
