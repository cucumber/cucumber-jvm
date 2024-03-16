package io.cucumber.junit.platform.engine;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.plugin.event.Node;

import java.util.Locale;
import java.util.function.Supplier;

enum DefaultNamingStrategy implements NamingStrategy {
    LONG {
        @Override
        public String name(Node node) {
            return longStrategy(node, nameOrKeyword(node));
        }
    },

    LONG_WITH_PICKLE_NAME {
        @Override
        public String name(Node node) {
            return longStrategy(node, pickleName(node));
        }
    },

    SHORT {
        @Override
        public String name(Node node) {
            return nameOrKeyword(node);
        }
    },

    SHORT_WITH_PICKLE_NAME {
        @Override
        public String name(Node node) {
            return pickleName(node);
        }
    };

    static DefaultNamingStrategy getStrategy(String s) {
        return valueOf(s.toUpperCase(Locale.ROOT));
    }

    NamingStrategy specify(String exampleStrategy) {
        switch (exampleStrategy) {
            case "example-number":
                return this;
            case "pickle-name":
                return this == LONG ? LONG_WITH_PICKLE_NAME : SHORT_WITH_PICKLE_NAME;
            default:
                throw new IllegalArgumentException("Unrecognized example NamingStrategy " + exampleStrategy);
        }
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

    private static String pickleName(Node node) {
        if (!(node instanceof Node.Example)) {
            return nameOrKeyword(node);
        }

        Node parent = node;
        do {
            parent = parent.getParent().orElse(null);
        } while (!(parent instanceof Feature) && parent != null);

        if (parent == null) {
            return nameOrKeyword(node);
        }
        return ((Feature) parent).getPickleAt(node).getName();
    }
}
