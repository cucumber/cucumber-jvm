package cucumber.api.datatable;

import cucumber.stepexpression.TableParser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TableDifferTest {

    private DataTable table() {
        String source = "" +
                "| Aslak | aslak@email.com | 123 |\n" +
                "| Joe   | joe@email.com   | 234 |\n" +
                "| Bryan | bryan@email.org | 456 |\n" +
                "| Ni    | ni@email.com    | 654 |\n";
        return TableParser.parse(source);
    }

    private DataTable tableWithDuplicate() {
        String source = "" +
                "| Aslak | aslak@email.com | 123 |\n" +
                "| Joe   | joe@email.com   | 234 |\n" +
                "| Bryan | bryan@email.org | 456 |\n" +
                "| Joe   | joe@email.com   | 234 |\n" +
                "| Ni    | ni@email.com    | 654 |\n" +
                "| Ni    | ni@email.com    | 654 |\n" ;
        return TableParser.parse(source);
    }

    private DataTable otherTableWithTwoConsecutiveRowsDeleted() {
        String source = "" +
                "| Aslak | aslak@email.com | 123 |\n" +
                "| Ni    | ni@email.com    | 654 |\n";
        return TableParser.parse(source);

    }

    private DataTable otherTableWithTwoConsecutiveRowsChanged() {
        String source = "" +
                "| Aslak | aslak@email.com  | 123 |\n" +
                "| Joe   | joe@NOSPAM.com   | 234 |\n" +
                "| Bryan | bryan@NOSPAM.org | 456 |\n" +
                "| Ni    | ni@email.com     | 654 |\n";
        return TableParser.parse(source);
    }

    private DataTable otherTableWithTwoConsecutiveRowsInserted() {
        String source = "" +
                "| Aslak | aslak@email.com      | 123 |\n" +
                "| Joe   | joe@email.com        | 234 |\n" +
                "| Doe   | joe@email.com        | 234 |\n" +
                "| Foo   | schnickens@email.net | 789 |\n" +
                "| Bryan | bryan@email.org      | 456 |\n" +
                "| Ni    | ni@email.com         | 654 |\n";
        return TableParser.parse(source);
    }

    private DataTable otherTableWithDeletedAndInserted() {
        String source = "" +
                "| Aslak | aslak@email.com      | 123 |\n" +
                "| Doe   | joe@email.com        | 234 |\n" +
                "| Foo   | schnickens@email.net | 789 |\n" +
                "| Bryan | bryan@email.org      | 456 |\n";
        return TableParser.parse(source);
    }

    private DataTable otherTableWithInsertedAtEnd() {
        String source = "" +
                "| Aslak | aslak@email.com      | 123 |\n" +
                "| Joe   | joe@email.com        | 234 |\n" +
                "| Bryan | bryan@email.org      | 456 |\n" +
                "| Ni    | ni@email.com         | 654 |\n" +
                "| Doe   | joe@email.com        | 234 |\n" +
                "| Foo   | schnickens@email.net | 789 |\n";
        return TableParser.parse(source);
    }

    private DataTable otherTableWithDifferentOrder() {
        String source = "" +
                "| Joe   | joe@email.com   | 234 |\n" +
                "| Aslak | aslak@email.com | 123 |\n" +
                "| Bryan | bryan@email.org | 456 |\n" +
                "| Ni    | ni@email.com    | 654 |\n";
        return TableParser.parse(source);
    }

    private DataTable otherTableWithDifferentOrderAndDuplicate() {
        String source = "" +
                "| Joe   | joe@email.com   | 234 |\n" +
                "| Aslak | aslak@email.com | 123 |\n" +
                "| Bryan | bryan@email.org | 456 |\n" +
                "| Ni    | ni@email.com    | 654 |\n"+
                "| Ni    | ni@email.com    | 654 |\n" +
                "| Joe   | joe@email.com   | 234 |\n" ;
        return TableParser.parse(source);
    }

    private DataTable  otherTableWithDifferentOrderDuplicateAndDeleted() {
        String source = "" +
                "| Joe   | joe@email.com   | 234 |\n" +
                "| Bryan | bryan@email.org | 456 |\n" +
                "| Bryan | bryan@email.org | 456 |\n" +
                "| Ni    | ni@email.com    | 654 |\n" +
                "| Bob   | bob.email.com   | 555 |\n" +
                "| Bryan | bryan@email.org | 456 |\n" +
                "| Ni    | ni@email.com    | 654 |\n" +
                "| Joe   | joe@email.com   | 234 |\n" ;

        return TableParser.parse(source);
    }

    private DataTable otherTableWithDeletedAndInsertedDifferentOrder() {
        String source = "" +
                "| Doe   | joe@email.com        | 234 |\n" +
                "| Foo   | schnickens@email.net | 789 |\n" +
                "| Aslak | aslak@email.com      | 123 |\n" +
                "| Bryan | bryan@email.org      | 456 |\n";
        return TableParser.parse(source);
    }

    @Test(expected = TableDiffException.class)
    public void shouldFindDifferences() {
        try {
            DataTable otherTable = otherTableWithDeletedAndInserted();
            new TableDiffer(table(), otherTable).calculateDiffs();
        } catch (TableDiffException e) {
            String expected = "" +
                    "Tables were not identical:\n" +
                    "      | Aslak | aslak@email.com      | 123 |\n" +
                    "    - | Joe   | joe@email.com        | 234 |\n" +
                    "    + | Doe   | joe@email.com        | 234 |\n" +
                    "    + | Foo   | schnickens@email.net | 789 |\n" +
                    "      | Bryan | bryan@email.org      | 456 |\n" +
                    "    - | Ni    | ni@email.com         | 654 |\n";
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    @Test(expected = TableDiffException.class)
    public void shouldFindNewLinesAtEnd() {
        try {
            new TableDiffer(table(), otherTableWithInsertedAtEnd()).calculateDiffs();
        } catch (TableDiffException e) {
            String expected = "" +
                    "Tables were not identical:\n" +
                    "      | Aslak | aslak@email.com      | 123 |\n" +
                    "      | Joe   | joe@email.com        | 234 |\n" +
                    "      | Bryan | bryan@email.org      | 456 |\n" +
                    "      | Ni    | ni@email.com         | 654 |\n" +
                    "    + | Doe   | joe@email.com        | 234 |\n" +
                    "    + | Foo   | schnickens@email.net | 789 |\n";
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    @Test
    public void considers_same_table_as_equal() {
        diff(table(), table());
    }

    @Test(expected = TableDiffException.class)
    public void should_find_new_lines_at_end_when_using_diff() {
        try {
            DataTable other = otherTableWithInsertedAtEnd();
            diff(table(), other);
        } catch (TableDiffException e) {
            String expected = "" +
                    "Tables were not identical:\n" +
                    "      | Aslak | aslak@email.com      | 123 |\n" +
                    "      | Joe   | joe@email.com        | 234 |\n" +
                    "      | Bryan | bryan@email.org      | 456 |\n" +
                    "      | Ni    | ni@email.com         | 654 |\n" +
                    "    + | Doe   | joe@email.com        | 234 |\n" +
                    "    + | Foo   | schnickens@email.net | 789 |\n";
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    @Test(expected = TableDiffException.class)
    public void should_not_fail_with_out_of_memory() {
        DataTable expected = TableParser.parse("" +
                "| I'm going to work |\n");
        List<List<String>> actual = new ArrayList<List<String>>();
        actual.add(asList("I just woke up"));
        actual.add(asList("I'm going to work"));
        diff(expected, DataTable.create(actual));
    }

    @Test(expected = TableDiffException.class)
    public void should_diff_when_consecutive_deleted_lines() {
        try {
            DataTable other = otherTableWithTwoConsecutiveRowsDeleted();
            diff(table(), other);
        } catch (TableDiffException e) {
            String expected = "" +
                    "Tables were not identical:\n" +
                    "      | Aslak | aslak@email.com | 123 |\n" +
                    "    - | Joe   | joe@email.com   | 234 |\n" +
                    "    - | Bryan | bryan@email.org | 456 |\n" +
                    "      | Ni    | ni@email.com    | 654 |\n";
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    @Test(expected = TableDiffException.class)
    public void should_diff_with_empty_list() {
        try {
            List<List<String>> other = new ArrayList<List<String>>();
            diff(table(), DataTable.create(other));
        } catch (TableDiffException e) {
            String expected = "" +
                    "Tables were not identical:\n" +
                    "    - | Aslak | aslak@email.com | 123 |\n" +
                    "    - | Joe   | joe@email.com   | 234 |\n" +
                    "    - | Bryan | bryan@email.org | 456 |\n" +
                    "    - | Ni    | ni@email.com    | 654 |\n";
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    @Test(expected = TableDiffException.class)
    public void should_diff_with_empty_table() {
        try {
            DataTable emptyTable = DataTable.emptyDataTable();
            diff(table(), emptyTable);
        } catch (TableDiffException e) {
            String expected = "" +
                "Tables were not identical:\n" +
                "    - | Aslak | aslak@email.com | 123 |\n" +
                "    - | Joe   | joe@email.com   | 234 |\n" +
                "    - | Bryan | bryan@email.org | 456 |\n" +
                "    - | Ni    | ni@email.com    | 654 |\n";
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    @Test
    public void empty_list_should_not_diff_with_empty_table() {
        List<List<String>> emptyList = new ArrayList<List<String>>();
        DataTable emptyTable = DataTable.emptyDataTable();
        assertEquals(emptyTable.raw(), emptyList);
    }

    @Test(expected = TableDiffException.class)
    public void should_diff_when_consecutive_changed_lines() {
        try {
            DataTable other = otherTableWithTwoConsecutiveRowsChanged();
            diff(table(), other);
        } catch (TableDiffException e) {
            String expected = "" +
                    "Tables were not identical:\n" +
                    "      | Aslak | aslak@email.com  | 123 |\n" +
                    "    - | Joe   | joe@email.com    | 234 |\n" +
                    "    - | Bryan | bryan@email.org  | 456 |\n" +
                    "    + | Joe   | joe@NOSPAM.com   | 234 |\n" +
                    "    + | Bryan | bryan@NOSPAM.org | 456 |\n" +
                    "      | Ni    | ni@email.com     | 654 |\n";
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    @Test(expected = TableDiffException.class)
    public void should_diff_when_consecutive_inserted_lines() {
        try {
            DataTable other = otherTableWithTwoConsecutiveRowsInserted();
            diff(table(), other);
        } catch (TableDiffException e) {
            String expected = "" +
                    "Tables were not identical:\n" +
                    "      | Aslak | aslak@email.com      | 123 |\n" +
                    "      | Joe   | joe@email.com        | 234 |\n" +
                    "    + | Doe   | joe@email.com        | 234 |\n" +
                    "    + | Foo   | schnickens@email.net | 789 |\n" +
                    "      | Bryan | bryan@email.org      | 456 |\n" +
                    "      | Ni    | ni@email.com         | 654 |\n";
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    @Test(expected = TableDiffException.class)
    public void should_return_tables() {
        DataTable table = table();
        DataTable other = otherTableWithTwoConsecutiveRowsInserted();
        try {
            diff(table, other);
        } catch (TableDiffException e) {
            String expected = "" +
                    "      | Aslak | aslak@email.com      | 123 |\n" +
                    "      | Joe   | joe@email.com        | 234 |\n" +
                    "    + | Doe   | joe@email.com        | 234 |\n" +
                    "    + | Foo   | schnickens@email.net | 789 |\n" +
                    "      | Bryan | bryan@email.org      | 456 |\n" +
                    "      | Ni    | ni@email.com         | 654 |\n";
            assertSame(table, e.getFrom());
            assertSame(other, e.getTo());
            assertEquals(expected, e.getDiff().toString());
            throw e;
        }
    }

    public static class TestPojo {
        private final Integer id;
        private final String givenName;
        private final int decisionCriteria;

        public TestPojo(Integer id, String givenName, int decisionCriteria) {
            this.id = id;
            this.givenName = givenName;
            this.decisionCriteria = decisionCriteria;
        }

        public Integer getId() {
            return id;
        }

        public String getGivenName() {
            return givenName;
        }

        public int getDecisionCriteria() {
            return decisionCriteria;
        }
    }

    @Test
    public void diff_set_with_itself() {
        unorderedDiff(table(), table());
    }

    @Test
    public void diff_set_with_itself_in_different_order() {
        DataTable other = otherTableWithDifferentOrder();
        unorderedDiff(table(), other);
    }

    @Test(expected = TableDiffException.class)
    public void diff_set_with_less_lines_in_other() {
        DataTable other = otherTableWithTwoConsecutiveRowsDeleted();
        try {
            unorderedDiff(table(), other);
        } catch (TableDiffException e) {
            String expected = "" +
                    "Tables were not identical:\n" +
                    "      | Aslak | aslak@email.com | 123 |\n" +
                    "    - | Joe   | joe@email.com   | 234 |\n" +
                    "    - | Bryan | bryan@email.org | 456 |\n" +
                    "      | Ni    | ni@email.com    | 654 |\n";
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    @Test(expected = TableDiffException.class)
    public void unordered_diff_with_more_lines_in_other() {
        DataTable other = otherTableWithTwoConsecutiveRowsInserted();
        try {
            unorderedDiff(table(), other);
        } catch (TableDiffException e) {
            String expected = "" +
                    "Tables were not identical:\n" +
                    "      | Aslak | aslak@email.com      | 123 |\n" +
                    "      | Joe   | joe@email.com        | 234 |\n" +
                    "      | Bryan | bryan@email.org      | 456 |\n" +
                    "      | Ni    | ni@email.com         | 654 |\n" +
                    "    + | Doe   | joe@email.com        | 234 |\n" +
                    "    + | Foo   | schnickens@email.net | 789 |\n";
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    @Test(expected = TableDiffException.class)
    public void unordered_diff_with_added_and_deleted_rows_in_other() {
        DataTable other = otherTableWithDeletedAndInsertedDifferentOrder();
        try {
            unorderedDiff(table(), other);
        } catch (TableDiffException e) {
            String expected = "" +
                    "Tables were not identical:\n" +
                    "      | Aslak | aslak@email.com      | 123 |\n" +
                    "    - | Joe   | joe@email.com        | 234 |\n" +
                    "      | Bryan | bryan@email.org      | 456 |\n" +
                    "    - | Ni    | ni@email.com         | 654 |\n" +
                    "    + | Doe   | joe@email.com        | 234 |\n" +
                    "    + | Foo   | schnickens@email.net | 789 |\n";
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    @Test(expected = TableDiffException.class)
    public void unordered_diff_with_added_duplicate_in_other() {
        DataTable other = otherTableWithDifferentOrderAndDuplicate();
        try {
            unorderedDiff(table(), other);
        } catch (TableDiffException e) {
            String expected = "" +
                    "Tables were not identical:\n" +
                    "      | Aslak | aslak@email.com | 123 |\n" +
                    "      | Joe   | joe@email.com   | 234 |\n" +
                    "      | Bryan | bryan@email.org | 456 |\n" +
                    "      | Ni    | ni@email.com    | 654 |\n" +
                    "    + | Ni    | ni@email.com    | 654 |\n" +
                    "    + | Joe   | joe@email.com   | 234 |\n" ;
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    @Test(expected = TableDiffException.class)
    public void unordered_diff_with_added_duplicate_and_deleted_in_other() {
        DataTable other = otherTableWithDifferentOrderDuplicateAndDeleted();
        try {
            unorderedDiff(tableWithDuplicate(), other);
        } catch (TableDiffException e) {
            String expected = "" +
                    "Tables were not identical:\n" +
                    "    - | Aslak | aslak@email.com | 123 |\n" +
                    "      | Joe   | joe@email.com   | 234 |\n" +
                    "      | Bryan | bryan@email.org | 456 |\n" +
                    "      | Joe   | joe@email.com   | 234 |\n" +
                    "      | Ni    | ni@email.com    | 654 |\n" +
                    "      | Ni    | ni@email.com    | 654 |\n" +
                    "    + | Bryan | bryan@email.org | 456 |\n" +
                    "    + | Bob   | bob.email.com   | 555 |\n" +
                    "    + | Bryan | bryan@email.org | 456 |\n" ;
            assertEquals(expected, e.getMessage());
            throw e;
        }
    }

    private void unorderedDiff(DataTable table, DataTable other) {
        new TableDiffer(table, other).calculateUnorderedDiffs();
    }

    private static void diff(DataTable table, DataTable other) {
        new TableDiffer(table, other).calculateDiffs();
    }

}
