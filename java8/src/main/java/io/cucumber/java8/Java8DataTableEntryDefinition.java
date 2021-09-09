package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.datatable.DataTableType;

import java.util.Map;

final class Java8DataTableEntryDefinition extends AbstractDatatableElementTransformerDefinition
        implements DataTableTypeDefinition {

    private final DataTableType dataTableType;

    Java8DataTableEntryDefinition(String[] emptyPatterns, DataTableEntryDefinitionBody<?> body) {
        super(body, new Exception().getStackTrace()[3], emptyPatterns);
        Class<?> returnType = resolveRawArguments(DataTableEntryDefinitionBody.class, body.getClass())[0];
        this.dataTableType = new DataTableType(
            returnType,
            (Map<String, String> entry) -> invokeMethod(replaceEmptyPatternsWithEmptyString(entry)));
    }

    @Override
    public DataTableType dataTableType() {
        return dataTableType;
    }

}
