package cucumber.table;

import cucumber.runtime.converters.LocalizedXStreams;
import gherkin.I18n;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.lexer.Lexer;
import gherkin.lexer.Listener;
import gherkin.lexer.i18n.EN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableParser {
    private static final List<Comment> NO_COMMENTS = Collections.emptyList();

    public static DataTable parse(String source) {
        final List<DataTableRow> rows = new ArrayList<DataTableRow>();
        Lexer l = new EN(new Listener() {
            @Override
            public void comment(String comment, Integer line) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void tag(String tag, Integer line) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void feature(String keyword, String name, String description, Integer line) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void background(String keyword, String name, String description, Integer line) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void scenario(String keyword, String name, String description, Integer line) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void scenarioOutline(String keyword, String name, String description, Integer line) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void examples(String keyword, String name, String description, Integer line) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void step(String keyword, String name, Integer line) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void row(List<String> cells, Integer line) {
                rows.add(new DataTableRow(NO_COMMENTS, cells, line));
            }

            @Override
            public void docString(String contentType, String string, Integer line) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void eof() {
            }
        });
        l.scan(source);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return new DataTable(rows, new TableConverter(new LocalizedXStreams(classLoader).get(new I18n("en")), null));
    }
}
