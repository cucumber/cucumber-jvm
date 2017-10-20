package cucumber.runtime.table;

import cucumber.api.DataTable;
import cucumber.api.TableConverter;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleTable;

import java.util.List;

import static cucumber.runtime.PickleTableConverter.toTable;

public class TableParser {

    private TableParser() {
    }

    public static DataTable parse(String source, TableConverter tableConverter) {
        String feature = "" +
                "Feature:\n" +
                "  Scenario:\n" +
                "    Given x\n" +
                source;
        Parser<GherkinDocument> parser = new Parser<GherkinDocument>(new AstBuilder());
        Compiler compiler = new Compiler();
        List<Pickle> pickles = compiler.compile(parser.parse(feature));
        PickleTable pickleTable = (PickleTable)pickles.get(0).getSteps().get(0).getArgument().get(0);
        return new DataTable(toTable(pickleTable), tableConverter);
    }
}
