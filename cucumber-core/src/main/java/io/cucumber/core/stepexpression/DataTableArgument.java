package io.cucumber.core.stepexpression;

import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableFormatter;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static java.util.Objects.requireNonNull;

public final class DataTableArgument implements Argument {

    private final int argumentIndex;
    private final RawTableTransformer<?> transformer;
    private final List<List<String>> argument;

    DataTableArgument(int argumentIndex, RawTableTransformer<?> transformer, List<List<@Nullable String>> argument) {
        this.argumentIndex = argumentIndex;
        this.transformer = requireNonNull(transformer);
        this.argument = requireNonNull(argument);
    }

    @Override
    public @Nullable Object getValue() {
        return transformer.transform(argumentIndex, argument);
    }

    @Override
    public String toString() {
        return "Table:\n" + getText();
    }

    private String getText() {
        return DataTableFormatter.builder()
                .prefixRow("      ")
                .build()
                .format(DataTable.create(argument));
    }

}
