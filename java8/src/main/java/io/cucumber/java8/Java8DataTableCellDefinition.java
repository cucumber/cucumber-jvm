package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.datatable.DataTableType;

final class Java8DataTableCellDefinition extends AbstractDatatableElementTransformerDefinition implements DataTableTypeDefinition {

    private final DataTableType dataTableType;

    Java8DataTableCellDefinition(String[] emptyPatterns, DataTableCellDefinitionBody<?> body) {
        super(body, new Exception().getStackTrace()[3], emptyPatterns);
        Class<?> returnType = resolveRawArguments(DataTableCellDefinitionBody.class, body.getClass())[0];
        this.dataTableType = new DataTableType(
            returnType,
            (String cell) -> execute(replaceEmptyPatternsWithEmptyString(cell))
        );
    }

    private Object execute(Object cell) {
        return Invoker.invoke(this, body, method, cell);
    }

    @Override
    public DataTableType dataTableType() {
        return dataTableType;
    }

}
