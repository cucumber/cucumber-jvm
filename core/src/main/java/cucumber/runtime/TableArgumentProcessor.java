package cucumber.runtime;

import cucumber.table.Table;

/**
 * 
 * Implementations of this interface are responsible to process a table, for
 * instance to transform it to a list of objects
 * 
 */
public interface TableArgumentProcessor {
    Object process(Table table);
}
