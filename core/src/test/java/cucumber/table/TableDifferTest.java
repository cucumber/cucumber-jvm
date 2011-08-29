package cucumber.table;

import gherkin.formatter.PrettyFormatter;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

public class TableDifferTest {
    private static final String line_separator = System.getProperty("line.separator");

    private Table table() {
        String source = 
                "| name  | email                | credits |" + line_separator +
                "| Aslak | aslak@email.com      | 123     |" + line_separator +
                "| Joe   | joe@email.com        | 234     |" + line_separator +
                "| Bryan | bryan@email.org      | 456     |" + line_separator +
                "| Ni    | ni@email.com         | 654     |" + line_separator;
        return TableParser.parse(source);
    }

    private Table otherTableWithDeletedAndInserted() {
        String source = 
                "| name  | email                | credits |" + line_separator +
                "| Aslak | aslak@email.com      | 123     |" + line_separator +
                "| Doe   | joe@email.com        | 234     |" + line_separator +
                "| Foo   | schnickens@email.net | 789     |" + line_separator +
                "| Bryan | bryan@email.org      | 456     |" + line_separator;
        return TableParser.parse(source);
    }

    private Table otherTableWithInsertedAtEnd() {
        String source = 
                "| name  | email                | credits |" + line_separator +
                "| Aslak | aslak@email.com      | 123     |" + line_separator +
                "| Joe   | joe@email.com        | 234     |" + line_separator +
                "| Bryan | bryan@email.org      | 456     |" + line_separator +
                "| Ni    | ni@email.com         | 654     |" + line_separator +
                "| Doe   | joe@email.com        | 234     |" + line_separator +
                "| Foo   | schnickens@email.net | 789     |" + line_separator;
        return TableParser.parse(source);
    }

    @Test(expected = TableDiffException.class)
    public void shouldFindDifferences() {
        try {
            new TableDiffer(table(), otherTableWithDeletedAndInserted()).calculateDiffs();
        } catch (TableDiffException e) {
            String expected = 
                    "      | name | email                | credits |" + line_separator +
                    "    - | Joe  | joe@email.com        | 234     |" + line_separator +
                    "    + | Doe  | joe@email.com        | 234     |" + line_separator +
                    "    + | Foo  | schnickens@email.net | 789     |" + line_separator +
                    "      | Joe  | joe@email.com        | 234     |" + line_separator +
                    "    - | Ni   | ni@email.com         | 654     |" + line_separator;

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
                    "      | name  | email                | credits |" + line_separator +
                    "      | Aslak | aslak@email.com      | 123     |" + line_separator +
                    "      | Joe   | joe@email.com        | 234     |" + line_separator +
                    "      | Bryan | bryan@email.org      | 456     |" + line_separator +
                    "    + | Doe   | joe@email.com        | 234     |" + line_separator +
                    "    + | Foo   | schnickens@email.net | 789     |" + line_separator;

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
