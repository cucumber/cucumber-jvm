package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;

final class Java8DataTableDefinition extends AbstractDatatableElementTransformerDefinition implements DataTableTypeDefinition {

    private final DataTableType dataTableType;

    Java8DataTableDefinition(String[] emptyPatterns, DataTableDefinitionBody<?> body) {
        super(body, new Exception().getStackTrace()[3], emptyPatterns);
        Class<?> returnType = resolveRawArguments(DataTableDefinitionBody.class, body.getClass())[0];
        this.dataTableType = new DataTableType(
            returnType,
            (DataTable table) -> execute(replaceEmptyPatternsWithEmptyString(table))
        );
    }

    private Object execute(DataTable table) {
        return Invoker.invoke(this, body, method, table);
    }

    @Override
    public DataTableType dataTableType() {
        return dataTableType;
    }

}
