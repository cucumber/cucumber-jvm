package cucumber.api.datatable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

final class NoConverterDefined implements TableConverter {

    @Override
    public <T> T convert(DataTable dataTable, Type type, boolean transposed) {
        throw new CucumberDataTableException(String.format("Can't convert DataTable to %s. DataTable was created without a converter", type));
    }

    @Override
    public <T> List<T> toList(DataTable dataTable, Type itemType) {
        throw new CucumberDataTableException(String.format("Can't convert DataTable to List<%s>. DataTable was created without a converter", itemType));
    }

    @Override
    public <T> List<List<T>> toLists(DataTable dataTable, Type itemType) {
        throw new CucumberDataTableException(String.format("Can't convert DataTable to List<List<%s>>. DataTable was created without a converter", itemType));
    }

    @Override
    public <K, V> Map<K, V> toMap(DataTable dataTable, Type keyType, Type valueType) {
        throw new CucumberDataTableException(String.format("Can't convert DataTable to Map<%s,%s>. DataTable was created without a converter", keyType, valueType));
    }

    @Override
    public <K, V> List<Map<K, V>> toMaps(DataTable dataTable, Type keyType, Type valueType) {
        throw new CucumberDataTableException(String.format("Can't convert DataTable to List<Map<%s,%s>>. DataTable was created without a converter", keyType, valueType));
    }

}
