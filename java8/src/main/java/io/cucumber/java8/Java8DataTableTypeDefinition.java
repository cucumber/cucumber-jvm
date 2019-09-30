package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellTransformer;
import io.cucumber.datatable.TableEntryTransformer;
import io.cucumber.datatable.TableRowTransformer;
import io.cucumber.datatable.TableTransformer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import net.jodah.typetools.TypeResolver;

final class Java8DataTableTypeDefinition extends AbstractGlueDefinition implements DataTableTypeDefinition {

    private final DataTableType dataTableType;

    Java8DataTableTypeDefinition(Object body) {
        super(body, new Exception().getStackTrace()[3]);
        this.dataTableType = createDataTableType(method);
    }

    private DataTableType createDataTableType(Method method) {
        Class returnType = TypeResolver.resolveRawArguments(DataTableEntryDefinitionBody.class, body.getClass())[0];
        Type[] parameterTypes = method.getGenericParameterTypes();
        Type parameterType = parameterTypes[0];

        if (DataTable.class.equals(parameterType)) {
            return new DataTableType( returnType, (TableTransformer<Object>) this::execute);
        }

        if (List.class.equals(parameterType)) {
            return new DataTableType(returnType, (TableRowTransformer<Object>) this::execute);
        }

        if (Map.class.equals(parameterType)) {
            return new DataTableType(returnType, (TableEntryTransformer<Object>) this::execute);
        }

        if (String.class.equals(parameterType)) {
            return new DataTableType(returnType, (TableCellTransformer<Object>) this::execute);
        }

        return new DataTableType(returnType, (TableTransformer<Object>) this::execute);
    }

    @Override
    public DataTableType dataTableType() {
        return dataTableType;
    }

    private Object execute(Object arg) throws Throwable {
        return Invoker.invoke(this, body, method, arg);
    }
}
