package io.cucumber.junit;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class FileNameCompatibleNames {

    static String createName(String name, Integer uniqueSuffix, boolean useFilenameCompatibleNames) {
        if (uniqueSuffix == null) {
            return createName(name, useFilenameCompatibleNames);
        }
        return createName(name + " #" + uniqueSuffix + "", useFilenameCompatibleNames);
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

    static <V, K> Integer uniqueSuffix(Map<K, List<V>> groupedByName, V pickle, Function<V, K> nameOf) {
        List<V> withSameName = groupedByName.get(nameOf.apply(pickle));
        boolean makeNameUnique = withSameName.size() > 1;
        return makeNameUnique ? withSameName.indexOf(pickle) + 1 : null;
    }

}
