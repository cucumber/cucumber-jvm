package io.cucumber.java;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.reflection.MethodFormat;
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

import static io.cucumber.java.InvalidMethodSignatureExceptionBuilder.builder;

class JavaDataTableTypeDefinition implements DataTableTypeDefinition {

    private final Method method;
    private final Lookup lookup;
    private final DataTableType dataTableType;
    private final String shortFormat;
    private final String fullFormat;

    JavaDataTableTypeDefinition(Method method, Lookup lookup) {
        this.method = method;
        this.lookup = lookup;
        this.dataTableType = createDataTableType(method);
        this.shortFormat = MethodFormat.SHORT.format(method);
        this.fullFormat = MethodFormat.FULL.format(method);
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

        throw createInvalidSignatureException(method);

    }

    private static CucumberException createInvalidSignatureException(Method method) {
        return builder(method)
            .addAnnotation(DataTableType.class)
            .addSignature("public Author author(DataTable table)")
            .addSignature("public Author author(List<String> row)")
            .addSignature("public Author author(Map<String, String> entry)")
            .addSignature("public Author author(String cell)")
            .addNote("Note: Author is an example of the class you want to convert the table to")
            .build();
    }


    private static Type requireValidParameterType(Method method) {
        Type[] parameterTypes = method.getGenericParameterTypes();
        if (parameterTypes.length != 1) {
            throw createInvalidSignatureException(method);
        }

        Type parameterType = parameterTypes[0];
        if (!(parameterType instanceof ParameterizedType)) {
            return parameterType;
        }

        ParameterizedType parameterizedType = (ParameterizedType) parameterType;
        Type[] typeParameters = parameterizedType.getActualTypeArguments();
        for (Type typeParameter : typeParameters) {
            if (!String.class.equals(typeParameter)) {
                throw createInvalidSignatureException(method);
            }
        }

        return parameterizedType.getRawType();
    }

    private static Class requireValidReturnType(Method method) {
        Class returnType = method.getReturnType();
        if (Void.class.equals(returnType)) {
            throw createInvalidSignatureException(method);
        }
        return returnType;
    }


    @Override
    public DataTableType dataTableType() {
        return dataTableType;
    }

    @Override
    public String getLocation(boolean detail) {
        return detail ? fullFormat : shortFormat;
    }

    private Object execute(Object arg) throws Throwable {
        return Invoker.invoke(lookup.getInstance(method.getDeclaringClass()), method, 0, arg);
    }

}
