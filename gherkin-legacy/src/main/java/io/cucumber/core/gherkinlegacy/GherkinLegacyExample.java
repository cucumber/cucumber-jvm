package io.cucumber.core.gherkinlegacy;

import gherkin.ast.TableRow;
import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Location;

import static io.cucumber.core.gherkinlegacy.GherkinLegacyLocation.from;

public final class GherkinLegacyExample implements Example {

    private final TableRow tableRow;
    private final int rowIndex;

    GherkinLegacyExample(TableRow tableRow, int rowIndex) {
        this.tableRow = tableRow;
        this.rowIndex = rowIndex;
    }

    @Override
    public String getKeyWord() {
        return null;
    }

    public String getName() {
        return "Example #" + rowIndex;
    }

    @Override
    public Location getLocation() {
        return from(tableRow.getLocation());
    }
}
