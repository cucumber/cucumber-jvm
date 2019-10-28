package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableTransformer;

import static net.jodah.typetools.TypeResolver.resolveRawArguments;

final class Java8DataTableDefinition extends AbstractGlueDefinition implements DataTableTypeDefinition {

    private final DataTableType dataTableType;

    Java8DataTableDefinition(DataTableDefinitionBody body) {
        super(body, new Exception().getStackTrace()[3]);
        Class returnType = resolveRawArguments(DataTableDefinitionBody.class, body.getClass())[0];
        this.dataTableType = new DataTableType(returnType, (TableTransformer<Object>) this::execute);
    }

    @Override
    public DataTableType dataTableType() {
        return dataTableType;
    }

    private Object execute(Object arg) {
        return Invoker.invoke(this, body, method, arg);
    }
}
