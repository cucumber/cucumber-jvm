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
import static io.cucumber.junit.platform.engine.Constants.JUNIT_PLATFORM_SUREFIRE_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME;

enum DefaultNamingStrategyProvider {
    LONG {
        @Override
        NamingStrategy create(ConfigurationParameters configuration) {
            return configuration.get(JUNIT_PLATFORM_LONG_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME)
                    .map(DefaultNamingStrategyProvider::parseStrategy)
                    .orElse(DefaultNamingStrategyProvider::exampleNumberAndPickleIfParameterizedStrategy)
                    .apply(DefaultNamingStrategyProvider::longStrategy);
        }
    },

    SHORT {
        @Override
        NamingStrategy create(ConfigurationParameters configuration) {
            return configuration.get(JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME)
                    .map(DefaultNamingStrategyProvider::parseStrategy)
                    .orElse(DefaultNamingStrategyProvider::exampleNumberAndPickleIfParameterizedStrategy)
                    .apply(DefaultNamingStrategyProvider::shortStrategy);
        }
    },

    SUREFIRE {
        @Override
        NamingStrategy create(ConfigurationParameters configuration) {
            return configuration.get(JUNIT_PLATFORM_SUREFIRE_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME)
                    .map(DefaultNamingStrategyProvider::parseStrategy)
                    .orElse(DefaultNamingStrategyProvider::exampleNumberAndPickleIfParameterizedStrategy)
                    .apply(DefaultNamingStrategyProvider::surefireStrategy);
        }
    };

    abstract NamingStrategy create(ConfigurationParameters configuration);

    static DefaultNamingStrategyProvider getStrategyProvider(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }

    private static Function<BiFunction<Node, String, String>, NamingStrategy> parseStrategy(String exampleStrategy) {
        switch (exampleStrategy) {
            case "number":
                return DefaultNamingStrategyProvider::exampleNumberStrategy;
            case "number-and-pickle-if-parameterized":
                return DefaultNamingStrategyProvider::exampleNumberAndPickleIfParameterizedStrategy;
            case "pickle":
                return DefaultNamingStrategyProvider::pickleNameStrategy;
            default:
                throw new IllegalArgumentException("Unrecognized example naming strategy " + exampleStrategy);
        }
    }

    private static NamingStrategy exampleNumberAndPickleIfParameterizedStrategy(
            BiFunction<Node, String, String> baseStrategy
    ) {
        return createNamingStrategy(
            (node) -> baseStrategy.apply(node, nameOrKeyword(node)),
            (node, pickle) -> baseStrategy.apply(node, nameOrKeyword(node) + pickleNameIfParameterized(node, pickle)));
    }

    private static String pickleNameIfParameterized(Node node, Pickle pickle) {
        if (node instanceof Node.Example) {
            String pickleName = pickle.getName();
            boolean parameterized = !node.getParent()
                    .flatMap(Node::getParent)
                    .flatMap(Node::getName)
                    .filter(pickleName::equals)
                    .isPresent();
            if (parameterized) {
                return ": " + pickleName;
            }
        }
        return "";
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
            public String nameExample(Node node, Pickle pickle) {
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

    private static String surefireStrategy(Node node, String currentNodeName) {
        // Surefire uses the parents of test nodes to determine the class name.
        // As we want the class name to match the feature name we name the
        // parents of the test containing nodes after the feature.
        if (node instanceof Node.Examples || node instanceof Node.Rule) {
            return nameOrKeyword(findFeature(node));
        }
        // Scenarios and examples names are used by surefire to populate the
        // testname. We want this to be long, but without the feature name
        // because that will be used for the class name
        if (node instanceof Node.Scenario || node instanceof Node.Example) {
            return longStrategyWithoutFeatureName(node, currentNodeName);
        }
        // Everything else, can be short, will be ignored by surefire.
        return shortStrategy(node, currentNodeName);
    }

    private static String longStrategyWithoutFeatureName(Node node, String currentNodeName) {
        StringBuilder builder = new StringBuilder();
        builder.append(currentNodeName);
        node = node.getParent().orElse(null);

        while (node != null && !(node instanceof Node.Feature)) {
            builder.insert(0, " - ");
            builder.insert(0, nameOrKeyword(node));
            node = node.getParent().orElse(null);
        }
        return builder.toString();
    }

    private static Node findFeature(Node node) {
        Node candidate = node.getParent().orElse(null);
        while (candidate != null) {
            node = candidate;
            candidate = node.getParent().orElse(null);
        }
        return node;
    }
}
