package io.cucumber.java8;

import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.datatable.TableCellByTypeTransformer;

import java.lang.reflect.Type;

class Java8DefaultDataTableCellTransformerDefinition extends AbstractDatatableElementTransformerDefinition implements DefaultDataTableCellTransformerDefinition {

    Java8DefaultDataTableCellTransformerDefinition(String[] emptyPatterns, DefaultDataTableCellTransformerBody body) {
        super(body, new Exception().getStackTrace()[3], emptyPatterns);
    }

    @Override
    public TableCellByTypeTransformer tableCellByTypeTransformer() {
        return (fromValue, toValueType) -> execute(
            replaceEmptyPatternsWithEmptyString(fromValue),
            toValueType
        );
    }

    private Object execute(String fromValue, Type toValueType) {
        return Invoker.invoke(this, body, method, fromValue, toValueType);
    }

}
