package cucumber.table;

import gherkin.formatter.PrettyFormatter;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TableDifferTest {
    private static final String EOL = System.getProperty("line.separator");

    private Table table() {
        String source =
                "| Aslak | aslak@email.com      | 123     |" + EOL +
                "| Joe   | joe@email.com        | 234     |" + EOL +
                "| Bryan | bryan@email.org      | 456     |" + EOL +
                "| Ni    | ni@email.com         | 654     |" + EOL;
        return TableParser.parse(source);
    }

    private Table otherTableWithDeletedAndInserted() {
        String source =
                "| Aslak | aslak@email.com      | 123 |" + EOL +
                "| Doe   | joe@email.com        | 234 |" + EOL +
                "| Foo   | schnickens@email.net | 789 |" + EOL +
                "| Bryan | bryan@email.org      | 456 |" + EOL;
        return TableParser.parse(source);
    }

    private Table otherTableWithInsertedAtEnd() {
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
            assertEquals(expected, pretty(e.getDiffTable()));
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
            assertEquals(expected, pretty(e.getDiffTable()));
            throw e;
        }
    }

    private String pretty(Table table) {
        StringBuilder result = new StringBuilder();
        PrettyFormatter pf = new PrettyFormatter(result, true, false);
        pf.table(table.getGherkinRows());
        pf.eof();
        return result.toString();
    }
}
