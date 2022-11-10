package io.cucumber.core.options;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.StandardPickleOrders;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PickleOrderParser {

    private static final Logger log = LoggerFactory.getLogger(PickleOrderParser.class);

    private static final Pattern RANDOM_AND_SEED_PATTERN = Pattern.compile("random(?::(\\d+))?");

    private static final String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    private static final Pattern FULLY_QUALIFIED_CLASS_NAME = Pattern.compile(ID_PATTERN + "(\\." + ID_PATTERN + ")*");

    private static final String ERROR_MESSAGE = "Invalid order. Must be either reverse, random, random:<long> or fully qualified name of a class implementing PickleOrder interface.";

    static PickleOrder parse(String argument) {
        if ("reverse".equals(argument)) {
            return StandardPickleOrders.reverseLexicalUriOrder();
        }

        if ("lexical".equals(argument)) {
            return StandardPickleOrders.lexicalUriOrder();
        }

        Matcher matcher = RANDOM_AND_SEED_PATTERN.matcher(argument);
        if (matcher.matches()) {
            return StandardPickleOrders.random(parseSeed(matcher));
        }

        if (FULLY_QUALIFIED_CLASS_NAME.matcher(argument).matches()) {
            try {
                return tryCreateInstance(argument);
            } catch (Exception e) {
                throw new IllegalArgumentException(ERROR_MESSAGE, e);
            }
        }

        throw new IllegalArgumentException(ERROR_MESSAGE);
    }

    private static PickleOrder tryLoadUsingServiceProvider(String argument) {
        ServiceLoader<PickleOrder> loader = ServiceLoader.load(PickleOrder.class, PickleOrderParser.class.getClassLoader());
        return loader.stream().filter( it -> it.getClass().getName().equals(argument)).findFirst().orElseThrow().get();
    }

    private static PickleOrder tryCreateInstance(String argument) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return (PickleOrder) Class.forName(argument).getConstructor().newInstance();
    }

    private static long parseSeed(Matcher matcher) {
        final long seed;
        String seedString = matcher.group(1);
        if (seedString != null) {
            seed = Long.parseLong(seedString);
        } else {
            seed = Math.abs(new Random().nextLong());
            log.info(() -> "Using random scenario order. Seed: " + seed);
        }
        return seed;
    }

}
