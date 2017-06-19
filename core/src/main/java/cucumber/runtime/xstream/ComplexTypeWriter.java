package cucumber.runtime.xstream;


import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.runtime.CucumberException;
import cucumber.runtime.table.CamelCaseStringConverter;
import cucumber.runtime.table.PascalCaseStringConverter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static java.util.Arrays.asList;

public class ComplexTypeWriter extends CellWriter {
    private final List<String> columnNames;
    private final Map<String, String> fields = new LinkedHashMap<String, String>();
    private final Stack<String> currentKey = new Stack<String>();

    public ComplexTypeWriter(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    @Override
    public List<String> getHeader() {
        return columnNames.isEmpty() ? new ArrayList<String>(fields.keySet()) : columnNames;
    }

    @Override
    public List<String> getValues() {
        CamelCaseStringConverter converter = new CamelCaseStringConverter();
        if (!columnNames.isEmpty()) {
            String[] explicitFieldValues = new String[columnNames.size()];
            int n = 0;
            for (String columnName : columnNames) {
                final String convertedColumnName = converter.map(columnName);
                if (fields.containsKey(convertedColumnName)) {
                    explicitFieldValues[n] = fields.get(convertedColumnName);
                } else {
                    explicitFieldValues[n] = "";
                }
                n++;
            }
            return asList(explicitFieldValues);
        } else {
            return new ArrayList<String>(fields.values());
        }
    }

    @Override
    public void startNode(String name) {
        currentKey.push(name);
        if (currentKey.size() == 2) {
            fields.put(name, "");
        }
    }

    @Override
    public void addAttribute(String name, String value) {
    }

    @Override
    public void setValue(String value) {
        // Add all simple types at level 2. nodeDepth 1 is the root node.
        if(currentKey.size() < 2){
            return;
        }

        if (currentKey.size() == 2) {
            fields.put(currentKey.peek(), value == null ? "" : value);
            return;
        }

        final String clazz = currentKey.get(0);
        final String field = currentKey.get(1);
        if ((columnNames.isEmpty() || columnNames.contains(field))) {
            throw createMissingConverterException(clazz, field);
        }
    }

    @Override
    public void endNode() {
        currentKey.pop();
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    private static CucumberException createMissingConverterException(String clazz, String field) {
        PascalCaseStringConverter converter = new PascalCaseStringConverter();
        return new CucumberException(String.format(
            "Don't know how to convert \"%s.%s\" into a table entry.\n" +
                "Either exclude %s from the table by selecting the fields to include:\n" +
                "\n" +
                "DataTable.create(entries, \"Field\", \"Other Field\")\n" +
                "\n" +
                "Or try writing your own converter:\n" +
                "\n" +
                "@%s(%sConverter.class)\n" +
                "%s %s;\n",
            clazz,
            field,
            field,
            XStreamConverter.class.getName(),
            converter.map(field),
            modifierAndTypeOfField(clazz, field),
            field
        ));
    }

    private static String modifierAndTypeOfField(String clazz, String fieldName) {
        try {
            Field field = Class.forName(clazz).getDeclaredField(fieldName);
            String simpleTypeName = field.getType().getSimpleName();
            String modifiers = Modifier.toString(field.getModifiers());
            return modifiers + " " + simpleTypeName;
        } catch (NoSuchFieldException e) {
            return "private Object";
        } catch (ClassNotFoundException e) {
            return "private Object";
        }
    }
}
