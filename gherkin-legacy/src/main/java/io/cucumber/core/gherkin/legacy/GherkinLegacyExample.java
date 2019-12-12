package io.cucumber.core.gherkin.legacy;

import gherkin.ast.TableRow;
import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Location;

import static io.cucumber.core.gherkin.legacy.GherkinLegacyLocation.from;

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
        return GherkinLegacyLocation.from(tableRow.getLocation());
    }
}
