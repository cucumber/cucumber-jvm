package io.cucumber.stepexpression;

import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTable.TableConverter;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleTable;

import java.util.List;

import static io.cucumber.stepexpression.PickleTableConverter.toTable;

public class TableParser {

    private TableParser() {
    }

    public static DataTable parse(String source) {
        return parse(source, null);
    }

    public static DataTable parse(String source, TableConverter converter) {
        String feature = "" +
            "Feature:\n" +
            "  Scenario:\n" +
            "    Given x\n" +
            source;
        Parser<GherkinDocument> parser = new Parser<GherkinDocument>(new AstBuilder());
        Compiler compiler = new Compiler();
        List<Pickle> pickles = compiler.compile(parser.parse(feature));
        PickleTable pickleTable = (PickleTable) pickles.get(0).getSteps().get(0).getArgument().get(0);
        if (converter == null) {
            return DataTable.create(toTable(pickleTable));
        }
        return DataTable.create(toTable(pickleTable), converter);
    }
}
