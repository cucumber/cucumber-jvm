package io.cucumber.core.options;

import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.factory.PickleOrderFactory;

import java.util.ServiceLoader;
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
        ServiceLoader<PickleOrderFactory> loader = ServiceLoader.load(PickleOrderFactory.class,
            PickleOrderParser.class.getClassLoader());
        // TODO: better exception, show choices, sync with tests
        PickleOrderFactory pickleOrderFactory = loader.stream().filter(it -> it.get().getName().equals(name))
                .findFirst()
                .orElseThrow().get();
        return pickleOrderFactory.create(argument);
    }

}
