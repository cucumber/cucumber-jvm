package io.cucumber.junit.platform.engine;

import io.cucumber.plugin.event.Node;

import java.util.Locale;
import java.util.function.Supplier;

enum DefaultNamingStrategy implements NamingStrategy {

    LONG {
        @Override
        public String name(Node node) {
            StringBuilder builder = new StringBuilder();
            builder.append(nameOrKeyword(node));
            node = node.getParent().orElse(null);

            while (node != null) {
                builder.insert(0, " - ");
                builder.insert(0, nameOrKeyword(node));
                node = node.getParent().orElse(null);
            }

            return builder.toString();
        }
    },

    SHORT {
        @Override
        public String name(Node node) {
            return nameOrKeyword(node);
        }
    };

    static DefaultNamingStrategy getStrategy(String s) {
        return valueOf(s.toUpperCase(Locale.ROOT));
    }

    private static String nameOrKeyword(Node node) {
        Supplier<String> keyword = () -> node.getKeyword().orElse("Unknown");
        return node.getName().orElseGet(keyword);
    }

}
