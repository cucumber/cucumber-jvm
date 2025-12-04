package io.cucumber.core.stepexpression;

import org.jspecify.annotations.Nullable;

import java.util.List;

@FunctionalInterface
interface RawTableTransformer<T> {

    @Nullable T transform(List<List<String>> raw);

}
