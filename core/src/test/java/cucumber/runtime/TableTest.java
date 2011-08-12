package cucumber.runtime;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Row;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import cucumber.Table;

import static org.junit.Assert.assertTrue;

public class TableTest {
    
    @Test
    public void shouldHaveHeaders() {
        List<Row> rowsFromGherkin = new ArrayList<Row>();
        List<String> firstLine = Arrays.asList("one", "four", "seven");
        rowsFromGherkin.add(new Row(new ArrayList<Comment>(), firstLine, 1));
        rowsFromGherkin.add(new Row(new ArrayList<Comment>(), Arrays.asList("4444", "55555", "666666"), 2));
        Table table = new Table(rowsFromGherkin);
        assertTrue(table.getHeaders().containsAll(firstLine));
    }
    
}
