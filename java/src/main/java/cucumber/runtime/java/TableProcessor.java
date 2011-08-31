package cucumber.runtime.java;

import cucumber.table.Table;

public interface TableProcessor {
    Object process(Table table);
}
