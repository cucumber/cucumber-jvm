package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableRowTransformer;

import static net.jodah.typetools.TypeResolver.resolveRawArguments;

final class Java8DataTableRowDefinition extends AbstractGlueDefinition implements DataTableTypeDefinition {

    private final DataTableType dataTableType;

    Java8DataTableRowDefinition(DataTableRowDefinitionBody body) {
        super(body, new Exception().getStackTrace()[3]);
        Class returnType = resolveRawArguments(DataTableRowDefinitionBody.class, body.getClass())[0];
        this.dataTableType = new DataTableType(returnType, (TableRowTransformer<Object>) this::execute);
    }

    @Override
    public DataTableType dataTableType() {
        return dataTableType;
    }

    private Object execute(Object arg) {
        return Invoker.invoke(this, body, method, arg);
    }
}
