package io.cucumber.plugin.event;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

/**
 * Represents an argument in a cucumber or regular expressions
 * <p>
 * The step definition {@code I have {long} cukes in my belly} when matched with
 * {@code I have 7 cukes in my belly} will produce one argument with value
 * {@code "4"}, starting at {@code 7} and ending at {@code 8}.
 */
@API(status = API.Status.STABLE)
public interface Argument {

    String getParameterTypeName();

    @Nullable
    String getValue();

    int getStart();

    int getEnd();

    @Nullable
    Group getGroup();

}
