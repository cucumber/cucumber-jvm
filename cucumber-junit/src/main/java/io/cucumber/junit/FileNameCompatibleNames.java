package io.cucumber.junit;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyList;

final class FileNameCompatibleNames {

    private FileNameCompatibleNames() {
        /* no-op */
    }

    static String createName(String name, @Nullable Integer uniqueSuffix, boolean useFilenameCompatibleNames) {
        if (uniqueSuffix == null) {
            return createName(name, useFilenameCompatibleNames);
        }
        return createName(name + " #" + uniqueSuffix, useFilenameCompatibleNames);
    }

    static String createName(final String name, boolean useFilenameCompatibleNames) {
        if (useFilenameCompatibleNames) {
            return makeNameFilenameCompatible(name);
        }
        return name;
    }

    private static String makeNameFilenameCompatible(String name) {
        return name.replaceAll("[^A-Za-z0-9_]", "_");
    }

    static <V, K> @Nullable Integer uniqueSuffix(Map<K, List<V>> groupedByName, V pickle, Function<V, K> nameOf) {
        List<V> withSameName = groupedByName.getOrDefault(nameOf.apply(pickle), emptyList());
        boolean makeNameUnique = withSameName.size() > 1;
        return makeNameUnique ? withSameName.indexOf(pickle) + 1 : null;
    }

}
