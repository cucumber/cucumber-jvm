package cucumber.api.datatable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DataTableType implements Comparable<DataTableType> {

    private static final ConversionRequired CONVERSION_REQUIRED = new ConversionRequired();

    private final String name;
    private final Type type;
    private final RawTableTransformer<?> transformer;

    public <T> DataTableType(String name, Type type, RawTableTransformer<T> transformer) {
        if (name == null) throw new CucumberDataTableException("name cannot be null");
        if (type == null) throw new CucumberDataTableException("type cannot be null");
        if (transformer == null) throw new CucumberDataTableException("transformer cannot be null");
        this.name = name;
        this.type = type;
        this.transformer = transformer;
    }

    public <T> DataTableType(String name, Class<T> type, RawTableTransformer<T> transformer) {
        this(name, (Type) type, transformer);
    }


    public <T> DataTableType(String name, Class<T> type, final TableTransformer<T> transformer) {
        this(name, type, new RawTableTransformer<T>() {
            @Override
            public T transform(List<List<String>> raw) {
                return transformer.transform(new DataTable(raw, CONVERSION_REQUIRED));
            }
        });
    }

    public <T> DataTableType(String name, final Class<T> type, final TableRowTransformer<T> transformer) {
        this(name, aListOf(type), new RawTableTransformer<List<T>>() {
            @Override
            public List<T> transform(List<List<String>> raw) {
                DataTable table = new DataTable(raw, CONVERSION_REQUIRED);
                List<T> list = new ArrayList<T>();
                for (Map<String, String> tableRow : table.asMaps()) {
                    list.add(transformer.transform(tableRow));
                }

                return list;
            }
        });
    }

    public Object transform(List<List<String>> raw) {
        return transformer.transform(raw);
    }

    public int compareTo(DataTableType o) {
        return name.compareTo(o.name);
    }


    public String getName() {
        return name;
    }


    public Type getType() {
        return type;
    }

    public static Type aListOf(final Type type) {
        //TODO: Quick fake out. This works because we the parameter registry uses toString.
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{type};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }

            @Override
            public String toString() {
                if (type instanceof Class) {
                    return List.class.getName() + "<" + ((Class) type).getName() + ">";
                }

                return List.class.getName() + "<" + type.toString() + ">";
            }
        };
    }
}
