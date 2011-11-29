package cucumber.table;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TableDifferTest {
    private static final String EOL = System.getProperty("line.separator");

    private DataTable table() {
        String source =
                "| Aslak | aslak@email.com      | 123     |" + EOL +
                "| Joe   | joe@email.com        | 234     |" + EOL +
                "| Bryan | bryan@email.org      | 456     |" + EOL +
                "| Ni    | ni@email.com         | 654     |" + EOL;
        return TableParser.parse(source);
    }

    private DataTable otherTableWithDeletedAndInserted() {
        String source =
                "| Aslak | aslak@email.com      | 123 |" + EOL +
                "| Doe   | joe@email.com        | 234 |" + EOL +
                "| Foo   | schnickens@email.net | 789 |" + EOL +
                "| Bryan | bryan@email.org      | 456 |" + EOL;
        return TableParser.parse(source);
    }

    private DataTable otherTableWithInsertedAtEnd() {
        String source =
                "| Aslak | aslak@email.com      | 123 |" + EOL +
                "| Joe   | joe@email.com        | 234 |" + EOL +
                "| Bryan | bryan@email.org      | 456 |" + EOL +
                "| Ni    | ni@email.com         | 654 |" + EOL +
                "| Doe   | joe@email.com        | 234 |" + EOL +
                "| Foo   | schnickens@email.net | 789 |" + EOL;
        return TableParser.parse(source);
    }

    @Test(expected = TableDiffException.class)
    public void shouldFindDifferences() {
        try {
            new TableDiffer(table(), otherTableWithDeletedAndInserted()).calculateDiffs();
        } catch (TableDiffException e) {
            String expected =
                    "      | Aslak | aslak@email.com      | 123 |" + EOL +
                    "    - | Joe   | joe@email.com        | 234 |" + EOL +
                    "    + | Doe   | joe@email.com        | 234 |" + EOL +
                    "    + | Foo   | schnickens@email.net | 789 |" + EOL +
                    "      | Bryan | bryan@email.org      | 456 |" + EOL +
                    "    - | Ni    | ni@email.com         | 654 |" + EOL;
            assertEquals(expected, TableHelper.pretty(e.getDiffTable()));
            throw e;
        }
    }

    @Test(expected = TableDiffException.class)
    public void shouldFindNewLinesAtEnd() {
        try {
            new TableDiffer(table(), otherTableWithInsertedAtEnd()).calculateDiffs();
        } catch (TableDiffException e) {
            String expected =
                    "      | Aslak | aslak@email.com      | 123 |" + EOL +
                    "      | Joe   | joe@email.com        | 234 |" + EOL +
                    "      | Bryan | bryan@email.org      | 456 |" + EOL +
                    "      | Ni    | ni@email.com         | 654 |" + EOL +
                    "    + | Doe   | joe@email.com        | 234 |" + EOL +
                    "    + | Foo   | schnickens@email.net | 789 |" + EOL;
            assertEquals(expected, TableHelper.pretty(e.getDiffTable()));
            throw e;
        }
    }

}
