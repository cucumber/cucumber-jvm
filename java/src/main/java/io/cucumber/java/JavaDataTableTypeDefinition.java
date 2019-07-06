package io.cucumber.java;

import io.cucumber.core.backend.DataTableTypeTypeDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.runtime.Invoker;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellTransformer;
import io.cucumber.datatable.TableEntryTransformer;
import io.cucumber.datatable.TableRowTransformer;
import io.cucumber.datatable.TableTransformer;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

class JavaDataTableTypeDefinition implements DataTableTypeTypeDefinition {

    private final Method method;
    private final Lookup lookup;
    private final DataTableType dataTableType;

    JavaDataTableTypeDefinition(Method method, Lookup lookup) {
        this.method = method;
        this.lookup = lookup;
        this.dataTableType = createDataTableType(method);
    }

    @SuppressWarnings("unchecked")
    private DataTableType createDataTableType(Method method) {
        Class returnType = requireValidReturnType(method);
        Type parameterType = requireValidParameterType(method);

        if (DataTable.class.equals(parameterType)) {
            return new DataTableType(
                returnType,
                (TableTransformer<Object>) this::execute
            );
        }

        if (List.class.equals(parameterType)) {
            return new DataTableType(
                returnType,
                (TableRowTransformer<Object>) this::execute
            );
        }

        if (Map.class.equals(parameterType)) {
            return new DataTableType(
                returnType,
                (TableEntryTransformer<Object>) this::execute
            );
        }

        if (String.class.equals(parameterType)) {
            return new DataTableType(
                returnType,
                (TableCellTransformer<Object>) this::execute
            );
        }

        throw createInvalidSignatureException();

    }

    private static CucumberException createInvalidSignatureException() {
        return new CucumberException("" +
            "A @DataTableType annotated method must have one of these signatures:\n" +
            " * public Author author(DataTable table)\n" +
            " * public Author author(List<String> row)\n" +
            " * public Author author(Map<String, String> entry)\n" +
            " * public Author author(String cell)\n" +
            "Note: Author is an example of the class you want to convert the table to"
        );
    }


    private static Type requireValidParameterType(Method method) {
        Type[] parameterTypes = method.getGenericParameterTypes();
        if (parameterTypes.length != 1) {
            throw createInvalidSignatureException();
        }

        Type parameterType = parameterTypes[0];
        if (!(parameterType instanceof ParameterizedType)) {
            return parameterType;
        }

        ParameterizedType parameterizedType = (ParameterizedType) parameterType;
        Type[] typeParameters = parameterizedType.getActualTypeArguments();
        for (Type typeParameter : typeParameters) {
            if (!String.class.equals(typeParameter)) {
                throw createInvalidSignatureException();
            }
        }

        return parameterizedType.getRawType();
    }

    private static Class requireValidReturnType(Method method) {
        Class returnType = method.getReturnType();
        if (Void.class.equals(returnType)) {
            throw createInvalidSignatureException();
        }
        return returnType;
    }


    @Override
    public DataTableType dataTableType() {
        return dataTableType;
    }


    private Object execute(Object arg) throws Throwable {
        return Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, 0, arg);
    }

}
