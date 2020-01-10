package io.cucumber.java8;

import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.datatable.TableEntryByTypeTransformer;

import java.lang.reflect.Type;
import java.util.Map;

class Java8DefaultDataTableEntryTransformerDefinition extends AbstractDatatableElementTransformerDefinition implements DefaultDataTableEntryTransformerDefinition {

    Java8DefaultDataTableEntryTransformerDefinition(String[] emptyPatterns, DefaultDataTableEntryTransformerBody body) {
        super(body, new Exception().getStackTrace()[3], emptyPatterns);
    }

    @Override
    public TableEntryByTypeTransformer tableEntryByTypeTransformer() {
        return (fromValue, toValueType, tableCellByTypeTransformer) -> execute(
            replaceEmptyPatternsWithEmptyString(fromValue),
            toValueType
        );
    }

    private Object execute(Map<String, String> fromValue, Type toValueType) {
        return Invoker.invoke(this, body, method, fromValue, toValueType);
    }

    @Override
    public boolean headersToProperties() {
        return true;
    }
}
