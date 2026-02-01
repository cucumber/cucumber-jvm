package io.cucumber.datatable;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

/**
 * Transforms a single table cell to an instance of {@code T}.
 *
 * @param <T> the target type
 */
@API(status = API.Status.STABLE)
@FunctionalInterface
public interface TableCellTransformer<T> {

    /**
     * Transforms a single table cell to an instance of {@code T}.
     *
     * @param  cell      the contents of a cell.
     * @return           an instance of {@code T}
     * @throws Throwable when the transform fails for any reason
     */
    @Nullable
    T transform(@Nullable String cell) throws Throwable;
}
