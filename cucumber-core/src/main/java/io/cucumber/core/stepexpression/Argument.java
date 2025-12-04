package io.cucumber.core.stepexpression;

import org.jspecify.annotations.Nullable;

public interface Argument {

    @Nullable Object getValue();

    @Override
    String toString();

}
