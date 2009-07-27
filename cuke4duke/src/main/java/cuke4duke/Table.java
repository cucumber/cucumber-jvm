package cuke4duke;

import java.util.List;
import java.util.Map;

/**
 * Wrapper for Cucumber's native Cucumber::Ast::Table class.
 * Java step definitions can declare the last argument to be
 * of this type to receive a table object.
 */
public interface Table {
    public List<Map<String, String>> hashes();
    public void diffLists(List<List<String>> table);
    public void diffLists(List<List<String>> table, Map options);
    public void diffHashes(List<Map<String, String>> table);
    public void diffHashes(List<Map<String, String>> table, Map options);
}
