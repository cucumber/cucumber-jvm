package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;
import io.cucumber.datatable.TableTransformer;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import net.jodah.typetools.TypeResolver;

final class Java8DataTableTypeDefinition extends AbstractGlueDefinition implements DataTableTypeDefinition {
    
    private final DataTableType dataTableType;
    
    Java8DataTableTypeDefinition(Object body) {
        super(body, new Exception().getStackTrace()[3]);
        this.dataTableType = createDataTableType(method);
    }

    private DataTableType createDataTableType(Method method) {
        Class returnType = TypeResolver.resolveRawArguments(DataTableDefinitionBody.class, body.getClass())[0];
        Type[] parameterTypes = method.getGenericParameterTypes();
        Type parameterType = parameterTypes[0];

        if (Map.class.equals(parameterType)) {
            return new DataTableType(
                returnType,
                (TableEntryTransformer<Object>) this::execute
            );
        }
        else {
            return new DataTableType(
                returnType,
                (TableTransformer<Object>) this::execute
            );
        }
    }

    @Override
    public DataTableType dataTableType() {
        return dataTableType;
    }

    private Object execute(Object arg) throws Throwable {
        return Invoker.invoke(this, body, method, arg);
    }
}
