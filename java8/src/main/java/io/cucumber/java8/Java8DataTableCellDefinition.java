package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellTransformer;
import net.jodah.typetools.TypeResolver;

final class Java8DataTableCellDefinition extends AbstractGlueDefinition implements DataTableTypeDefinition {

    private final DataTableType dataTableType;

    Java8DataTableCellDefinition(DataTableCellDefinitionBody body) {
        super(body, new Exception().getStackTrace()[3]);
        Class returnType = TypeResolver.resolveRawArguments(DataTableCellDefinitionBody.class, body.getClass())[0];
        this.dataTableType = new DataTableType(returnType, (TableCellTransformer<Object>) this::execute);
    }

    @Override
    public DataTableType dataTableType() {
        return dataTableType;
    }

    private Object execute(Object arg) {
        return Invoker.invoke(this, body, method, arg);
    }
}
