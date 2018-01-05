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
    private final boolean preferForTypeMatch;

    public DataTableType(String name, Type type, RawTableTransformer<?> transformer) {
        this(name, type, transformer, false);
    }

    public DataTableType(String name, Type type, RawTableTransformer<?> transformer, boolean preferForTypeMatch) {
        if (name == null) throw new CucumberDataTableException("name cannot be null");
        if (type == null) throw new CucumberDataTableException("type cannot be null");
        if (transformer == null) throw new CucumberDataTableException("transformer cannot be null");
        this.name = name;
        this.type = type;
        this.transformer = transformer;
        this.preferForTypeMatch = preferForTypeMatch;
    }

    public <T> DataTableType(String name, Class<T> type, RawTableTransformer<T> transformer) {
        this(name, (Type) type, transformer, false);
    }

    public <T> DataTableType(String name, Class<T> type, RawTableTransformer<T> transformer, boolean preferForTypeMatch) {
        this(name, (Type) type, transformer, preferForTypeMatch);
    }

    public <T> DataTableType(String name, Class<T> type, final TableTransformer<T> transformer) {
        this(name, type, transformer, false);
    }

    public <T> DataTableType(String name, Class<T> type, final TableTransformer<T> transformer, boolean preferForTypeMatch) {
        this(name, type, new RawTableTransformer<T>() {
            @Override
            public T transform(List<List<String>> raw) {
                return transformer.transform(new DataTable(raw, CONVERSION_REQUIRED));
            }
        }, preferForTypeMatch);
    }

    public <T> DataTableType(String name, final Class<T> type, final TableEntryTransformer<T> transformer) {
        this(name, type, transformer, false);
    }

    public <T> DataTableType(String name, final Class<T> type, final TableEntryTransformer<T> transformer, boolean preferForTypeMatch) {
        this(name, aListOf(type), new RawTableTransformer<List<T>>() {
            @Override
            public List<T> transform(List<List<String>> raw) {
                DataTable table = new DataTable(raw, CONVERSION_REQUIRED);
                List<T> list = new ArrayList<T>();
                for (Map<String, String> entry : table.asMaps()) {
                    list.add(transformer.transform(entry));
                }

                return list;
            }
        }, preferForTypeMatch);
    }

    public <T> DataTableType(String name, final Class<T> type, final TableRowTransformer<T> transformer) {
        this(name, type, transformer, false);
    }

    public <T> DataTableType(String name, final Class<T> type, final TableRowTransformer<T> transformer, boolean preferForTypeMatch) {
        this(name, aListOf(type), new RawTableTransformer<List<T>>() {
            @Override
            public List<T> transform(List<List<String>> raw) {
                List<T> list = new ArrayList<T>();
                for (List<String> tableRow : raw) {
                    list.add(transformer.transform(tableRow));
                }

                return list;
            }
        }, preferForTypeMatch);
    }

    public <T> DataTableType(String name, final Class<T> type, final TableCellTransformer<T> transformer) {
        this(name, type, transformer, false);
    }

    public <T> DataTableType(String name, final Class<T> type, final TableCellTransformer<T> transformer, boolean preferForTypeMatch) {
        this(name, aListOf(aListOf(type)), new RawTableTransformer<List<List<T>>>() {
            @Override
            public List<List<T>> transform(List<List<String>> raw) {
                List<List<T>> list = new ArrayList<List<T>>(raw.size());
                for (List<String> tableRow : raw) {
                    List<T> row = new ArrayList<T>(tableRow.size());
                    for (String entry : tableRow) {
                        row.add(transformer.transform(entry));
                    }
                    list.add(row);
                }
                return list;
            }
        }, preferForTypeMatch);
    }

    public Object transform(List<List<String>> raw) {
        return transformer.transform(raw);
    }

    @Override
    public int compareTo(DataTableType o) {
        if (preferForTypeMatch() && !o.preferForTypeMatch()) return -1;
        if (o.preferForTypeMatch() && !preferForTypeMatch()) return 1;
        return getName().compareTo(o.getName());
    }


    public String getName() {
        return name;
    }


    public Type getType() {
        return type;
    }

    public boolean preferForTypeMatch() {
        return preferForTypeMatch;
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
