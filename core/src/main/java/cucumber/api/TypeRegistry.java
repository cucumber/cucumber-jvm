package cucumber.api;

import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import cucumber.api.datatable.DataTableType;
import cucumber.api.datatable.DataTableTypeRegistry;

import java.lang.reflect.Type;
import java.util.Locale;

public final class TypeRegistry {

    private final ParameterTypeRegistry parameterTypeRegistry;

    private final DataTableTypeRegistry dataTableTypeRegistry;


    public TypeRegistry(Locale locale) {
        parameterTypeRegistry = new ParameterTypeRegistry(locale);
        dataTableTypeRegistry = new DataTableTypeRegistry();
    }

    public ParameterTypeRegistry parameterTypeRegistry() {
        return parameterTypeRegistry;
    }

    public void defineParameterType(ParameterType<?> parameterType) {
        parameterTypeRegistry.defineParameterType(parameterType);
    }

    public void defineDataTableType(DataTableType tableType) {
        dataTableTypeRegistry.defineDataTableType(tableType);
    }

    public <T> ParameterType<T> lookupParameterTypeByTypeName(String typeName) {
        return parameterTypeRegistry.lookupByTypeName(typeName);
    }

    public DataTableType lookupTableTypeByType(Type type) {
        return dataTableTypeRegistry.lookupTableTypeByType(type);
    }

    public DataTableType lookupTableTypeByName(String tableType) {
        return dataTableTypeRegistry.lookupTableTypeByName(tableType);
    }
}
