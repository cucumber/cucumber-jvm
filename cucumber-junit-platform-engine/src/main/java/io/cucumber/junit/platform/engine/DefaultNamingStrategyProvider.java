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
            String exampleStrategy = configuration.get(JUNIT_PLATFORM_LONG_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME).orElse("example-number");
            return createNamingStrategy(exampleStrategy,
                    node -> longStrategy(node, nameOrKeyword(node)),
                    (node, pickle) -> longStrategy(node, nameOrKeyword(node)),
                    (node, pickle) -> longStrategy(node, pickle.getName()));
        }
    },

    SHORT {
        @Override
        NamingStrategy create(ConfigurationParameters configuration) {
            String exampleStrategy = configuration.get(JUNIT_PLATFORM_SHORT_NAMING_STRATEGY_EXAMPLE_NAME_PROPERTY_NAME).orElse("example-number");
            return createNamingStrategy(exampleStrategy,
                    DefaultNamingStrategyProvider::nameOrKeyword,
                    (node, pickle) -> nameOrKeyword(node),
                    (node, pickle) -> pickle.getName()
            );
        }
    };

    abstract NamingStrategy create(ConfigurationParameters configuration);

    static DefaultNamingStrategyProvider getStrategyProvider(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }

    private static NamingStrategy createNamingStrategy(String exampleStrategy,
                                                       Function<Node, String> name,
                                                       BiFunction<Node, Pickle, String> exampleNameByNumber,
                                                       BiFunction<Node, Pickle, String> exampleNameByPickle) {
        BiFunction<Node, Pickle, String> exampleName;
        switch (exampleStrategy) {
            case "example-number":
                exampleName = exampleNameByNumber;
                break;
            case "pickle-name":
                exampleName = exampleNameByPickle;
                break;
            default:
                throw new IllegalArgumentException("Unrecognized example NamingStrategy " + exampleStrategy);
        }

        return new NamingStrategy() {
            @Override
            public String name(Node node) {
                return name.apply(node);
            }

            @Override
            public String nameExample(Node.Example node, Pickle pickle) {
                return exampleName.apply(node, pickle);
            }
        };
    }

    private static String nameOrKeyword(Node node) {
        Supplier<String> keyword = () -> node.getKeyword().orElse("Unknown");
        return node.getName().orElseGet(keyword);
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
