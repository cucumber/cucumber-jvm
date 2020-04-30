package io.cucumber.core.gherkin.vintage;

import gherkin.ast.TableRow;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

final class GherkinVintageExample implements Node.Example {

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
