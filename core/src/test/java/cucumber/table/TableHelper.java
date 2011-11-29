package cucumber.table;

import gherkin.formatter.PrettyFormatter;

public class TableHelper {
    public static String pretty(DataTable table) {
        StringBuilder result = new StringBuilder();
        PrettyFormatter pf = new PrettyFormatter(result, true, false);
        pf.table(table.getGherkinRows());
        pf.eof();
        return result.toString();
    }
}
