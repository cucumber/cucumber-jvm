package io.cucumber.datatable;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

final class DataTableCellByTypeTransformer implements TableCellByTypeTransformer {

    private final DataTableTypeRegistry dataTableTypeRegistry;

    DataTableCellByTypeTransformer(DataTableTypeRegistry dataTableTypeRegistry) {
        this.dataTableTypeRegistry = dataTableTypeRegistry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable Object transform(@Nullable String cellValue, Type toValueType) {
        DataTableType typeByType = dataTableTypeRegistry.lookupCellTypeByType(toValueType);
        if (typeByType == null) {
            throw new CucumberDataTableException("There is no DataTableType registered for cell type " + toValueType);
        }
        List<List<@Nullable String>> rawTable = singletonList(singletonList(cellValue));
        List<List<@Nullable Object>> transformed = (List<List<Object>>) requireNonNull(typeByType.transform(rawTable));
        return transformed.get(0).get(0);
    }
}
