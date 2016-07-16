package cucumber.runtime.table;

import cucumber.api.DataTable;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleTable;

import java.util.List;
import java.util.Locale;

public class TableParser {

    private TableParser() {
    }

    public static DataTable parse(String source, ParameterInfo parameterInfo) {
        String feature = "" +
                "Feature:\n" +
                "  Scenario:\n" +
                "    Given x\n" +
                source;
        Parser<GherkinDocument> parser = new Parser<GherkinDocument>(new AstBuilder());
        Compiler compiler = new Compiler();
        List<Pickle> pickles = compiler.compile(parser.parse(feature), "path");
        PickleTable pickleTable = (PickleTable)pickles.get(0).getSteps().get(0).getArgument().get(0);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return new DataTable(pickleTable, new TableConverter(new LocalizedXStreams(classLoader).get(Locale.US), parameterInfo));
    }
}
