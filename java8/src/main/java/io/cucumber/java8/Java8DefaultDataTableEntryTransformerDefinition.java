package io.cucumber.java8;

import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;

import java.lang.reflect.Type;
import java.util.Map;

class Java8DefaultDataTableEntryTransformerDefinition extends AbstractGlueDefinition implements DefaultDataTableEntryTransformerDefinition {

    Java8DefaultDataTableEntryTransformerDefinition(DefaultDataTableEntryTransformerBody body) {
        super(body, new Exception().getStackTrace()[3]);
    }

    @Override
    public TableEntryByTypeTransformer tableEntryByTypeTransformer() {
        return this::execute;
    }

    private Object execute(Map<String, String> fromValue, Type toValue, TableCellByTypeTransformer cellTransformer) {
        return Invoker.invoke(this, body, method, fromValue, toValue);
    }

    @Override
    public boolean headersToProperties() {
        return true;
    }
}
