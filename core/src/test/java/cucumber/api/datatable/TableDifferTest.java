package cucumber.api.datatable;

import cucumber.stepexpression.TableParser;
import org.junit.Ignore;
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
        table().diff(table());
    }

    @Test(expected = TableDiffException.class)
    public void should_find_new_lines_at_end_when_using_diff() {
        try {
            DataTable other = otherTableWithInsertedAtEnd();
            table().diff(other);
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
        expected.diff(DataTable.create(actual));
    }

    @Test(expected = TableDiffException.class)
    public void should_diff_when_consecutive_deleted_lines() {
        try {
            DataTable other = otherTableWithTwoConsecutiveRowsDeleted();
            table().diff(other);
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
            table().diff(DataTable.create(other));
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
            table().diff(emptyTable);
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
            table().diff(other);
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
            table().diff(other);
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
        DataTable from = table();
        DataTable to = otherTableWithTwoConsecutiveRowsInserted();
        try {
            from.diff(to);
        } catch (TableDiffException e) {
            String expected = "" +
                    "      | Aslak | aslak@email.com      | 123 |\n" +
                    "      | Joe   | joe@email.com        | 234 |\n" +
                    "    + | Doe   | joe@email.com        | 234 |\n" +
                    "    + | Foo   | schnickens@email.net | 789 |\n" +
                    "      | Bryan | bryan@email.org      | 456 |\n" +
                    "      | Ni    | ni@email.com         | 654 |\n";
            assertSame(from, e.getFrom());
            assertSame(to, e.getTo());
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
        table().unorderedDiff(table());
    }

    @Test
    public void diff_set_with_itself_in_different_order() {
        DataTable other = otherTableWithDifferentOrder();
        table().unorderedDiff(other);
    }

    @Test(expected = TableDiffException.class)
    public void diff_set_with_less_lines_in_other() {
        DataTable other = otherTableWithTwoConsecutiveRowsDeleted();
        try {
            table().unorderedDiff(other);
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
            table().unorderedDiff(other);
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
            table().unorderedDiff(other);
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

    @Test
    @Ignore //TODO: How?
    public void unordered_diff_with_list_of_pojos_and_camelcase_header_mapping() {
        String source = "" +
                "| id | Given Name |\n" +
                "| 1  | me   |\n" +
                "| 2  | you  |\n" +
                "| 3  | jdoe |\n";

        DataTable expected = TableParser.parse(source);

        List<TestPojo> actual = new ArrayList<TestPojo>();
        actual.add(new TestPojo(2, "you", 222));
        actual.add(new TestPojo(3, "jdoe", 34545));
        actual.add(new TestPojo(1, "me", 123));
//        expected.unorderedDiff(createTableConverter().toTable(actual));
    }

    @Test(expected = TableDiffException.class)
    public void unordered_diff_with_added_duplicate_in_other() {
        DataTable other = otherTableWithDifferentOrderAndDuplicate();
        try {
            table().unorderedDiff(other);
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
            tableWithDuplicate().unorderedDiff(other);
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
}
