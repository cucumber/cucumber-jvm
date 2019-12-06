package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Location;
import io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow;

final class Gherkin8Example implements Example {

    private final TableRow tableRow;
    private final int rowIndex;

    Gherkin8Example(TableRow tableRow, int rowIndex) {
        this.tableRow = tableRow;
        this.rowIndex = rowIndex;
    }

    @Override
    public String getKeyWord() {
        return null;
    }

    @Override
    public String getName() {
        return "Example #" + rowIndex;
    }

    @Override
    public Location getLocation() {
        return Gherkin8Location.from(tableRow.getLocation());
    }
}
