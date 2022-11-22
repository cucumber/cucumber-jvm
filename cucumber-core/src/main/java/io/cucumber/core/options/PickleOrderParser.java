package io.cucumber.core.options;

import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.factory.PickleOrderFactory;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class PickleOrderParser {

    private static final Pattern NAME_WITH_ARGUMENT_PATTERN = Pattern.compile("([a-z_]+)(?::(.*))?");

    static PickleOrder parse(String argument) {
        Matcher matcher = NAME_WITH_ARGUMENT_PATTERN.matcher(argument);
        if (matcher.matches()) {
            return parse(matcher.group(1), matcher.group(2));
        }
        return parse(argument, null);
    }

    static PickleOrder parse(String name, String argument) {
        ServiceLoader<PickleOrderFactory> loader = ServiceLoader.load(PickleOrderFactory.class,
            PickleOrderParser.class.getClassLoader());
        Optional<ServiceLoader.Provider<PickleOrderFactory>> optionalPickleOrderFactoryProvider = loader.stream()
                .filter(it -> it.get().getName().equals(name))
                .findFirst();
        if (optionalPickleOrderFactoryProvider.isEmpty()) {
            String names = loader.stream().map(it -> it.get().getName()).collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid order '" +
                    name + "'. Must be in form of <name>:<argument. Possible names are: " + names + ".");
        }
        return optionalPickleOrderFactoryProvider.get().get().create(argument);
    }

}
