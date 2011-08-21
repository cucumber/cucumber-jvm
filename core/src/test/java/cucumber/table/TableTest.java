package cucumber.table;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Row;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cucumber.runtime.transformers.IntegerTransformer;
import cucumber.table.SimpleTableHeaderMapper;
import cucumber.table.Table;
import cucumber.table.TableHeaderMapper;

public class TableTest {

    private List<Row> simpleRows;
    private Table simpleTable;

    @Before
    public void initSimpleTable() {
        this.simpleRows = new ArrayList<Row>();
        String[] firstLine = new String[] { "one", "four", "seven" };
        this.simpleRows.add(new Row(new ArrayList<Comment>(), Arrays.asList(firstLine), 1));
        this.simpleRows.add(new Row(new ArrayList<Comment>(), Arrays.asList("4444", "55555", "666666"), 2));
        this.simpleTable = new Table(this.simpleRows, Locale.getDefault());
    }

    @Test
    public void shouldHaveHeaders() {
        List<String> cells = this.simpleRows.get(0).getCells();
        List<String> headers = this.simpleTable.getHeaders();
        assertTrue("Header's items", headers.containsAll(cells));
        assertEquals("Header length", cells.size(), headers.size());
    }

    @Test
    public void rawShouldHaveThreeColumnsAndTwoRows() {
        List<List<String>> raw = this.simpleTable.raw();
        assertEquals("Rows size", 2, raw.size());
        for (List<String> list : raw) {
            assertEquals("Cols size: " + list, 3, list.size());
        }
    }

    @Test
    public void shouldHaveOneRow() {
        List<String> firstRow = this.simpleRows.get(1).getCells();
        List<List<String>> tableRows = this.simpleTable.rows();
        assertEquals("Rows", 1, tableRows.size());
        assertTrue("Rows' items", tableRows.get(0).containsAll(firstRow));
    }

    @Test
    public void shouldBeConvertibleToAListOfMap() {
        List<Map<String, Object>> hashes = this.simpleTable.hashes();
        assertEquals("Hashes", 1, hashes.size());
        Map<String, Object> hash = hashes.get(0);
        assertEquals("Hash First Col", "4444", hash.get("one"));
        assertEquals("Hash Second Col", "55555", hash.get("four"));
        assertEquals("Hash Third Col", "666666", hash.get("seven"));
    }

    @Test
    public void shouldAllowMappingColumns() {
        this.simpleTable.mapColumn("one", new IntegerTransformer());
        List<Map<String, Object>> hashes = this.simpleTable.hashes();
        assertEquals("Hashes", 1, hashes.size());
        Map<String, Object> hash = hashes.get(0);
        assertEquals("Hash First Col", 4444, hash.get("one"));
    }

    @Test
    public void shouldAllowMappingHeaders() {
        this.simpleTable.mapHeaders(getHeaderMappings());
        List<String> headers = this.simpleTable.getHeaders();
        assertTrue("Header's items", headers.containsAll(Arrays.asList("eins", "vier", "sieben")));
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
        this.simpleTable.mapHeaders(getHeaderMappings());
        List<Map<String, Object>> hashes = this.simpleTable.hashes();
        assertEquals("Hashes", 1, hashes.size());
        Map<String, Object> hash = hashes.get(0);
        assertEquals("Hash First Col", "4444", hash.get("eins"));
        assertEquals("Hash Second Col", "55555", hash.get("vier"));
        assertEquals("Hash Third Col", "666666", hash.get("sieben"));
    }
    
}
