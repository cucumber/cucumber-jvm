package cucumber.table;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;

public class TableDifferTest {

    private DataTable table() {
        String source = "" +
                "| Aslak | aslak@email.com | 123 |\n" +
                "| Joe   | joe@email.com   | 234 |\n" +
                "| Bryan | bryan@email.org | 456 |\n" +
                "| Ni    | ni@email.com    | 654 |\n";
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

        try {
            expected.diff(actual);
        } catch (TableDiffException e) {
            String expectedDiff = "" +
                    "Tables were not identical:\n" +
                    "    + | I just woke up |\n";
            assertEquals(expectedDiff, e.getMessage());
            throw e;
        }
    }
}
