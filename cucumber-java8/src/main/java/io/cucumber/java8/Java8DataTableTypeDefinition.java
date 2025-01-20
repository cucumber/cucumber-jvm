package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;

abstract class Java8DataTableTypeDefinition extends AbstractDatatableElementTransformerDefinition
        implements DataTableTypeDefinition {

    Java8DataTableTypeDefinition(Object body, StackTraceElement location, String[] emptyPatterns) {
        super(body, location, emptyPatterns);
    }
}
