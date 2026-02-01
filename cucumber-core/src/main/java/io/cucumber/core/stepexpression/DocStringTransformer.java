package io.cucumber.core.stepexpression;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
interface DocStringTransformer<T> {

    @Nullable
    T transform(String docString, @Nullable String contentType);

}
