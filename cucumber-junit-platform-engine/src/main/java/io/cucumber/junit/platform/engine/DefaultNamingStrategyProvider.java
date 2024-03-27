package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.plugin.event.Node;
import org.junit.platform.engine.ConfigurationParameters;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_LONG_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME;

enum DefaultNamingStrategyProvider {
    LONG {
        @Override
        NamingStrategy create(ConfigurationParameters configuration) {
            return configuration.get(JUNIT_PLATFORM_LONG_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME)
                    .map(DefaultNamingStrategyProvider::parseStrategy)
                    .orElse(DefaultNamingStrategyProvider::exampleNumberStrategy)
                    .apply(DefaultNamingStrategyProvider::longStrategy);
        }
    },

    SHORT {
        @Override
        NamingStrategy create(ConfigurationParameters configuration) {
            return configuration.get(JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME)
                    .map(DefaultNamingStrategyProvider::parseStrategy)
                    .orElse(DefaultNamingStrategyProvider::exampleNumberStrategy)
                    .apply(DefaultNamingStrategyProvider::shortStrategy);
        }
    };

    abstract NamingStrategy create(ConfigurationParameters configuration);

    static DefaultNamingStrategyProvider getStrategyProvider(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }

    private static Function<BiFunction<Node, String, String>, NamingStrategy> parseStrategy(String exampleStrategy) {
        switch (exampleStrategy) {
            case "example-number":
                return DefaultNamingStrategyProvider::exampleNumberStrategy;
            case "pickle-name":
                return DefaultNamingStrategyProvider::pickleNameStrategy;
            default:
                throw new IllegalArgumentException("Unrecognized example naming strategy " + exampleStrategy);
        }
    }

    private static NamingStrategy exampleNumberStrategy(BiFunction<Node, String, String> baseStrategy) {
        return createNamingStrategy(
            (node) -> baseStrategy.apply(node, nameOrKeyword(node)),
            (node, pickle) -> baseStrategy.apply(node, nameOrKeyword(node)));
    }

    private static NamingStrategy pickleNameStrategy(BiFunction<Node, String, String> baseStrategy) {
        return createNamingStrategy(
            (node) -> baseStrategy.apply(node, nameOrKeyword(node)),
            (node, pickle) -> baseStrategy.apply(node, pickle.getName()));
    }

    private static NamingStrategy createNamingStrategy(
            Function<Node, String> nameFunction, BiFunction<Node, Pickle, String> exampleNameFunction
    ) {
        return new NamingStrategy() {
            @Override
            public String name(Node node) {
                return nameFunction.apply(node);
            }

            @Override
            public String nameExample(Node.Example node, Pickle pickle) {
                return exampleNameFunction.apply(node, pickle);
            }
        };
    }

    private static String nameOrKeyword(Node node) {
        Supplier<String> keyword = () -> node.getKeyword().orElse("Unknown");
        return node.getName().orElseGet(keyword);
    }

    private static String shortStrategy(Node node, String currentNodeName) {
        return currentNodeName;
    }

    private static String longStrategy(Node node, String currentNodeName) {
        StringBuilder builder = new StringBuilder();
        builder.append(currentNodeName);
        node = node.getParent().orElse(null);

        while (node != null) {
            builder.insert(0, " - ");
            builder.insert(0, nameOrKeyword(node));
            node = node.getParent().orElse(null);
        }

        return builder.toString();
    }
}
