package cucumber.api.datatable;

import com.fasterxml.jackson.databind.JavaType;
import io.cucumber.cucumberexpressions.CucumberExpressionException;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static cucumber.api.datatable.TypeFactory.constructType;

public final class DataTableTypeRegistry {


    private final Map<String, DataTableType> tableTypeByName = new HashMap<String, DataTableType>();
    private final HashMap<JavaType, SortedSet<DataTableType>> tableTypeByType = new HashMap<JavaType, SortedSet<DataTableType>>();

    public DataTableTypeRegistry(Locale locale) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        final NumberParser numberParser = new NumberParser(numberFormat);

        defineDataTableType(new DataTableType("bigint", BigInteger.class, new TableCellTransformer<BigInteger>() {
            @Override
            public BigInteger transform(String cell) {
                return new BigInteger(cell);
            }
        }, true));

        defineDataTableType(new DataTableType("bigdecimal", BigDecimal.class, new TableCellTransformer<BigDecimal>() {
            @Override
            public BigDecimal transform(String cell) {
                return new BigDecimal(cell);
            }
        }, true));

        defineDataTableType(new DataTableType("byte", Byte.class, new TableCellTransformer<Byte>() {
            @Override
            public Byte transform(String cell) {
                return Byte.decode(cell);
            }
        }, true));

        defineDataTableType(new DataTableType("short", Short.class, new TableCellTransformer<Short>() {
            @Override
            public Short transform(String cell) {
                return Short.decode(cell);
            }
        }, true));

        defineDataTableType(new DataTableType("int", Integer.class, new TableCellTransformer<Integer>() {
            @Override
            public Integer transform(String cell) {
                return Integer.decode(cell);
            }
        }, true));

        defineDataTableType(new DataTableType("long", Long.class, new TableCellTransformer<Long>() {
            @Override
            public Long transform(String cell) {
                return Long.decode(cell);
            }
        }));

        defineDataTableType(new DataTableType("float", Float.class, new TableCellTransformer<Float>() {
            @Override
            public Float transform(String cell) {
                return numberParser.parseFloat(cell);
            }
        }, true));

        defineDataTableType(new DataTableType("double", Double.class, new TableCellTransformer<Double>() {
            @Override
            public Double transform(String cell) {
                return numberParser.parseDouble(cell);
            }
        }, true));

        defineDataTableType(new DataTableType("string", String.class, new TableCellTransformer<String>() {
            @Override
            public String transform(String cell) {
                return cell;
            }
        }, true));

    }

    public void defineDataTableType(DataTableType dataTableType) {
        if (tableTypeByName.containsKey(dataTableType.getName())) {
            throw new DuplicateTypeNameException(String.format("There is already a data table type with name %s", dataTableType.getName()));
        }

        tableTypeByName.put(dataTableType.getName(), dataTableType);

        if (tableTypeByType.get(dataTableType.getType()) == null) {
            tableTypeByType.put(dataTableType.getType(), new TreeSet<DataTableType>());
        }
        SortedSet<DataTableType> dataTableTypes = tableTypeByType.get(dataTableType.getType());
        if (!dataTableTypes.isEmpty() && dataTableTypes.first().preferForTypeMatch() && dataTableType.preferForTypeMatch()) {
            throw new CucumberExpressionException(String.format(
                "There can only be one preferential data table type per type. " +
                    "The type %s is used for two preferential parameter types, {%s} and {%s}",
                dataTableType, dataTableTypes.first().getName(), dataTableType.getName()
            ));
        }
        dataTableTypes.add(dataTableType);
    }

    public DataTableType lookupTableTypeByType(final Type tableType) {
        SortedSet<DataTableType> dataTableTypes = tableTypeByType.get(constructType(tableType));
        if (dataTableTypes == null) return null;
        if (dataTableTypes.size() > 1 && !dataTableTypes.first().preferForTypeMatch()) {
            throw new CucumberExpressionException("TODO: Throw ambigous exception");
        }
        return dataTableTypes.first();
    }

    public DataTableType lookupTableTypeByName(String tableName) {
        return tableTypeByName.get(tableName);
    }

}


