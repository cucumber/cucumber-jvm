package io.cucumber.java8;

import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.datatable.TableCellByTypeTransformer;

import java.lang.reflect.Type;

class Java8DefaultDataTableCellTransformerDefinition extends AbstractGlueDefinition implements DefaultDataTableCellTransformerDefinition {

    Java8DefaultDataTableCellTransformerDefinition(DefaultDataTableCellTransformerBody body) {
        super(body, new Exception().getStackTrace()[3]);
    }

    @Override
    public TableCellByTypeTransformer tableCellByTypeTransformer() {
        return this::execute;
    }

    private Object execute(String fromValue, Type toValue) {
        return Invoker.invoke(this, body, method, fromValue, toValue);
    }

}
