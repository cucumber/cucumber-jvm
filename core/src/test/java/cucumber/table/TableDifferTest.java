package cucumber.table;

import gherkin.formatter.PrettyFormatter;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class TableDifferTest {
    private Table table() {
        String source =
                "| name  | email                | credits |\n" +
                        "| Aslak | aslak@email.com      | 123     |\n" +
                        "| Joe   | joe@email.com        | 234     |\n" +
                        "| Bryan | bryan@email.org      | 456     |\n" +
                        "| Ni    | ni@email.com         | 654     |\n";
        return TableParser.parse(source);
    }

    private Table otherTableWithDeletedAndInserted() {
        String source =
                "| name  | email                | credits |\n" +
                        "| Aslak | aslak@email.com      | 123     |\n" +
                        "| Doe   | joe@email.com        | 234     |\n" +
                        "| Foo   | schnickens@email.net | 789     |\n" +
                        "| Bryan | bryan@email.org      | 456     |\n";
        return TableParser.parse(source);
    }

    private Table otherTableWithInsertedAtEnd() {
        String source =
                "| name  | email                | credits |\n" +
                        "| Aslak | aslak@email.com      | 123     |\n" +
                        "| Joe   | joe@email.com        | 234     |\n" +
                        "| Bryan | bryan@email.org      | 456     |\n" +
                        "| Ni    | ni@email.com         | 654     |\n" +
                        "| Doe   | joe@email.com        | 234     |\n" +
                        "| Foo   | schnickens@email.net | 789     |\n";
        return TableParser.parse(source);
    }

    @Test(expected = TableDiffException.class)
    public void shouldFindDifferences() {
        try {
            new TableDiffer(table(), otherTableWithDeletedAndInserted()).calculateDiffs();
        } catch (TableDiffException e) {
            String expected =
                    "      | name | email                | credits |\n" +
                            "    - | Joe  | joe@email.com        | 234     |\n" +
                            "    + | Doe  | joe@email.com        | 234     |\n" +
                            "    + | Foo  | schnickens@email.net | 789     |\n" +
                            "      | Joe  | joe@email.com        | 234     |\n" +
                            "    - | Ni   | ni@email.com         | 654     |\n";

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
                    "      | name  | email                | credits |\n" +
                            "      | Aslak | aslak@email.com      | 123     |\n" +
                            "      | Joe   | joe@email.com        | 234     |\n" +
                            "      | Bryan | bryan@email.org      | 456     |\n" +
                            "    + | Doe   | joe@email.com        | 234     |\n" +
                            "    + | Foo   | schnickens@email.net | 789     |\n";

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
