package io.cucumber.core.gherkin.vintage;

import gherkin.ast.TableRow;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.util.Optional;

final class GherkinVintageExample implements Node.Example {

    private final TableRow tableRow;
    private final int rowIndex;

    GherkinVintageExample(TableRow tableRow, int rowIndex) {
        this.tableRow = tableRow;
        this.rowIndex = rowIndex;
    }

    @Override
    public Optional<String> getKeyWord() {
        return Optional.empty();
    }

    public Optional<String> getName() {
        return Optional.of("Example #" + rowIndex);
    }

    @Override
    public Location getLocation() {
        return GherkinVintageLocation.from(tableRow.getLocation());
    }
}
