package cuke4duke;

import java.util.List;
import java.util.Map;

/**
 * Java wrapper for some of Cucumber's Cucumber::Ast::Table methods.
 * Java step definitions can declare the last argument to be
 * of this type to receive a table object.
 *
 * See The <a href="http://wiki.github.com/aslakhellesoy/cucumber/rdoc">Cucumber::Ast::Table RDoc</a> for details.
 */
public interface Table {
    public List<Map<String, String>> hashes();
    public Map<String, String> rowsHash();
    public List<List<String>> raw();
    public List<List<String>> rows();
    public void diffLists(List<List<String>> table);
    public void diffLists(List<List<String>> table, Map<?, ?> options);
    public void diffHashes(List<Map<String, String>> table);
    public void diffHashes(List<Map<String, String>> table, Map<?, ?> options);
    public void convertColumn(String column, TableConverter converter);
    public void convertHeaders(TableConverter converter);
}
