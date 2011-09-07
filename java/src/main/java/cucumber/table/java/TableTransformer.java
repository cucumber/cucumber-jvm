package cucumber.table.java;

import cucumber.table.Table;

import java.util.List;

public interface TableTransformer {
    <T> List<T> transformTable(Table table);
}
