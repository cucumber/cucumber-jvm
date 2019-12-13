package io.cucumber.core.gherkin.vintage;

import gherkin.ast.TableRow;
import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Location;

final class GherkinVintageExample implements Example {

    private final TableRow tableRow;
    private final int rowIndex;

    GherkinVintageExample(TableRow tableRow, int rowIndex) {
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
        return GherkinVintageLocation.from(tableRow.getLocation());
    }
}
