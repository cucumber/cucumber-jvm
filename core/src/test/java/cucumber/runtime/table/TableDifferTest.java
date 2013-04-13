package cucumber.runtime.table;

import cucumber.api.DataTable;
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
        return TableParser.parse(source, null);
    }

    private DataTable otherTableWithTwoConsecutiveRowsDeleted() {
        String source = "" +
                "| Aslak | aslak@email.com | 123 |\n" +
                "| Ni    | ni@email.com    | 654 |\n";
        return TableParser.parse(source, null);

    }

    private DataTable otherTableWithTwoConsecutiveRowsChanged() {
        String source = "" +
                "| Aslak | aslak@email.com  | 123 |\n" +
                "| Joe   | joe@NOSPAM.com   | 234 |\n" +
                "| Bryan | bryan@NOSPAM.org | 456 |\n" +
                "| Ni    | ni@email.com     | 654 |\n";
        return TableParser.parse(source, null);
    }

    private DataTable otherTableWithTwoConsecutiveRowsInserted() {
        String source = "" +
                "| Aslak | aslak@email.com      | 123 |\n" +
                "| Joe   | joe@email.com        | 234 |\n" +
                "| Doe   | joe@email.com        | 234 |\n" +
                "| Foo   | schnickens@email.net | 789 |\n" +
                "| Bryan | bryan@email.org      | 456 |\n" +
                "| Ni    | ni@email.com         | 654 |\n";
        return TableParser.parse(source, null);
    }

    private DataTable otherTableWithDeletedAndInserted() {
        String source = "" +
                "| Aslak | aslak@email.com      | 123 |\n" +
                "| Doe   | joe@email.com        | 234 |\n" +
                "| Foo   | schnickens@email.net | 789 |\n" +
                "| Bryan | bryan@email.org      | 456 |\n";
        return TableParser.parse(source, null);
    }

    private DataTable otherTableWithInsertedAtEnd() {
        String source = "" +
                "| Aslak | aslak@email.com      | 123 |\n" +
                "| Joe   | joe@email.com        | 234 |\n" +
                "| Bryan | bryan@email.org      | 456 |\n" +
                "| Ni    | ni@email.com         | 654 |\n" +
                "| Doe   | joe@email.com        | 234 |\n" +
                "| Foo   | schnickens@email.net | 789 |\n";
        return TableParser.parse(source, null);
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
        table().diff(table().raw());
    }

    @Test(expected = TableDiffException.class)
    public void shouldFindNewLinesAtEndWhenUsingDiff() {
        try {
            List<List<String>> other = otherTableWithInsertedAtEnd().raw();
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
                "| I'm going to work |\n", null);
        List<List<String>> actual = new ArrayList<List<String>>();
        actual.add(asList("I just woke up"));
        actual.add(asList("I'm going to work"));
        expected.diff(actual);
    }

    @Test(expected = TableDiffException.class)
    public void should_diff_when_consecutive_deleted_lines() {
        try {
            List<List<String>> other = otherTableWithTwoConsecutiveRowsDeleted().raw();
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
    public void should_diff_when_consecutive_changed_lines() {
        try {
            List<List<String>> other = otherTableWithTwoConsecutiveRowsChanged().raw();
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
            List<List<String>> other = otherTableWithTwoConsecutiveRowsInserted().raw();
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
        Integer id;
        String givenName;
        int decisionCriteria;

        public TestPojo(Integer id, String givenName, int decisionCriteria) {
            this.id = id;
            this.givenName = givenName;
            this.decisionCriteria = decisionCriteria;
        }
    }

    @Test
    public void diff_with_list_of_pojos_and_camelcase_header_mapping() {
        String source = "" +
                "| id | Given Name |\n" +
                "| 1  | me   |\n" +
                "| 2  | you  |\n" +
                "| 3  | jdoe |\n";

        DataTable expected = TableParser.parse(source, null);

        List<TestPojo> actual = new ArrayList<TestPojo>();
        actual.add(new TestPojo(1, "me", 123));
        actual.add(new TestPojo(2, "you", 222));
        actual.add(new TestPojo(3, "jdoe", 34545));
        expected.diff(actual);
    }
}
