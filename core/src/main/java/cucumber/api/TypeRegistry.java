package cucumber.api;

import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.DataTableTypeRegistry;

import java.lang.reflect.Type;
import java.util.Locale;

public interface TypeRegistry {

    void defineParameterType(ParameterType<?> parameterType);

    void defineDataTableType(DataTableType tableType);

}
