package cucumber.table;

import java.util.List;

public interface TableTransformer {
    <T> List<T> transformTable(Table table);
}
