package cucumber.table;

import cucumber.runtime.transformers.IntegerTransformer;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Row;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TableTest {

    private List<Row> simpleRows;
    private Table simpleTable;

    @Before
    public void initSimpleTable() {
        simpleRows = new ArrayList<Row>();
        String[] firstLine = new String[]{"one", "four", "seven"};
        simpleRows.add(new Row(new ArrayList<Comment>(), asList(firstLine), 1));
        simpleRows.add(new Row(new ArrayList<Comment>(), asList("4444", "55555", "666666"), 2));
        simpleTable = new Table(simpleRows, Locale.getDefault());
    }

    @Test
    public void shouldHaveHeaders() {
        List<String> cells = simpleRows.get(0).getCells();
        List<String> headers = simpleTable.getHeaders();
        assertTrue("Header's items", headers.containsAll(cells));
        assertEquals("Header length", cells.size(), headers.size());
    }

    @Test
    public void rawShouldHaveThreeColumnsAndTwoRows() {
        List<List<String>> raw = simpleTable.raw();
        assertEquals("Rows size", 2, raw.size());
        for (List<String> list : raw) {
            assertEquals("Cols size: " + list, 3, list.size());
        }
    }

    @Test
    public void shouldHaveOneRow() {
        List<String> firstRow = simpleRows.get(1).getCells();
        List<List<Object>> tableRows = simpleTable.rows();
        assertEquals("Rows", 1, tableRows.size());
        assertTrue("Rows' items", tableRows.get(0).containsAll(firstRow));
    }

    @Test
    public void shouldBeConvertibleToAListOfMap() {
        List<Map<String, Object>> hashes = simpleTable.hashes();
        assertEquals("Hashes", 1, hashes.size());
        Map<String, Object> hash = hashes.get(0);
        assertEquals("Hash First Col", "4444", hash.get("one"));
        assertEquals("Hash Second Col", "55555", hash.get("four"));
        assertEquals("Hash Third Col", "666666", hash.get("seven"));
    }

    @Test
    public void shouldAllowMappingColumns() {
        simpleTable.mapColumn("one", new IntegerTransformer());
        List<Map<String, Object>> hashes = simpleTable.hashes();
        assertEquals("Hashes", 1, hashes.size());
        Map<String, Object> hash = hashes.get(0);
        assertEquals("Hash First Col", 4444, hash.get("one"));
    }

    @Test
    public void shouldAllowMappingHeaders() {
        simpleTable.mapHeaders(getHeaderMappings());
        List<String> headers = simpleTable.getHeaders();
        assertTrue("Header's items", headers.containsAll(asList("eins", "vier", "sieben")));
    }

    private TableHeaderMapper getHeaderMappings() {
        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("one", "eins");
        mappings.put("four", "vier");
        mappings.put("seven", "sieben");
        return new SimpleTableHeaderMapper(mappings);
    }

    @Test
    public void shouldBeConvertibleToAListOfMapAfterMapped() {
        simpleTable.mapHeaders(getHeaderMappings());
        List<Map<String, Object>> hashes = simpleTable.hashes();
        assertEquals("Hashes", 1, hashes.size());
        Map<String, Object> hash = hashes.get(0);
        assertEquals("Hash First Col", "4444", hash.get("eins"));
        assertEquals("Hash Second Col", "55555", hash.get("vier"));
        assertEquals("Hash Third Col", "666666", hash.get("sieben"));
    }

    @Test
    public void shouldAllowMappingColumnsByIndex() {
        simpleTable.mapColumn(1, new IntegerTransformer());
        List<Map<String, Object>> hashes = simpleTable.hashes();
        assertEquals("Hashes", 1, hashes.size());
        Map<String, Object> hash = hashes.get(0);
        assertEquals("Hash First Col", 55555, hash.get("four"));
    }

}
