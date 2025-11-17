package io.cucumber.datatable;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

final class NullMap {

    private NullMap() {
        // utility class
    }

    static Map<@Nullable Object, @Nullable Object> of(@Nullable Object... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide even number of arguments");
        }
        Map<@Nullable Object, @Nullable Object> map = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(values[i], values[i + 1]);
        }
        return map;
    }
}
