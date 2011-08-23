package cucumber.table;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Row;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class TableDifferTest {

    private Table origTable;
    private Table otherTable;
    private Table otherTableWithExtraRows;

    @Test(expected = TableDiffException.class)
    public void shouldFindDifferences() {
        try {
            new TableDiffer(this.origTable, this.otherTable).calculateDiffs();
        } catch (TableDiffException e) {
            List<RowDiff> rowDiffs = e.getTableDiff().getRowDiffs();
            Assert.assertEquals("Size of diff list", 6, rowDiffs.size());
            Assert.assertEquals("First row diff is NONE", DiffType.NONE, rowDiffs.get(0).getDiffType());
            Assert.assertEquals("Second row diff is DELETE", DiffType.DELETE, rowDiffs.get(1).getDiffType());
            Assert.assertEquals("Third row diff is INSERT", DiffType.INSERT, rowDiffs.get(2).getDiffType());
            Assert.assertEquals("Sixth row diff is DELETE", DiffType.DELETE, rowDiffs.get(5).getDiffType());
            throw e;
        }
    }

    @Test(expected = TableDiffException.class)
    public void shouldFindNewLinesAtEnd() {
        try {
            new TableDiffer(this.origTable, this.otherTableWithExtraRows).calculateDiffs();
        } catch (TableDiffException e) {
            List<RowDiff> rowDiffs = e.getTableDiff().getRowDiffs();
            Assert.assertEquals("Size of diff list", 6, rowDiffs.size());
            Assert.assertEquals("Third row diff is INSERT", DiffType.INSERT, rowDiffs.get(4).getDiffType());
            Assert.assertEquals("Third row diff is INSERT", DiffType.INSERT, rowDiffs.get(5).getDiffType());
            throw e;
        }
    }

    @Before
    public void createOtherTable() {
        List<Row> rows = new ArrayList<Row>();
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("name", "email", "credits"), 1));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Aslak", "aslak@email.com", "123"), 2));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Doe", "joe@email.com", "234"), 3));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Foo", "schnickens@email.net", "789"), 4));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Bryan", "bryan@email.org", "456"), 5));
        this.otherTable = new Table(rows, Locale.ENGLISH);
    }

    @Before
    public void createOrigTable() {
        List<Row> rows = new ArrayList<Row>();
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("name", "email", "credits"), 1));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Aslak", "aslak@email.com", "123"), 2));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Joe", "joe@email.com", "234"), 3));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Bryan", "bryan@email.org", "456"), 4));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Ni", "ni@email.com", "654"), 4));
        this.origTable = new Table(rows, Locale.ENGLISH);
    }

    @Before
    public void createOtherTableWithExtraRows() {
        List<Row> rows = new ArrayList<Row>();
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("name", "email", "credits"), 1));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Aslak", "aslak@email.com", "123"), 2));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Joe", "joe@email.com", "234"), 3));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Bryan", "bryan@email.org", "456"), 4));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Ni", "ni@email.com", "654"), 4));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Doe", "joe@email.com", "234"), 3));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Foo", "schnickens@email.net", "789"), 4));
        this.otherTableWithExtraRows = new Table(rows, Locale.ENGLISH);
    }

}
