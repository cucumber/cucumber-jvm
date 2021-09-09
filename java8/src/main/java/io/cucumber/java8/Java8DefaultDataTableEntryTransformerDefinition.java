package io.cucumber.java8;

import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.datatable.TableEntryByTypeTransformer;

class Java8DefaultDataTableEntryTransformerDefinition extends AbstractDatatableElementTransformerDefinition
        implements DefaultDataTableEntryTransformerDefinition {

    Java8DefaultDataTableEntryTransformerDefinition(String[] emptyPatterns, DefaultDataTableEntryTransformerBody body) {
        super(body, new Exception().getStackTrace()[3], emptyPatterns);
    }

    @Override
    public boolean headersToProperties() {
        return true;
    }

    @Override
    public TableEntryByTypeTransformer tableEntryByTypeTransformer() {
        return (fromValue, toValueType, tableCellByTypeTransformer) -> invokeMethod(
            replaceEmptyPatternsWithEmptyString(fromValue),
            toValueType);
    }
}
