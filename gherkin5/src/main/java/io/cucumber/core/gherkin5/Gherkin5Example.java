package io.cucumber.core.gherkin5;

import gherkin.ast.TableRow;
import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Location;

import static io.cucumber.core.gherkin5.Gherkin5Location.from;

public final class Gherkin5Example implements Example {

    private final TableRow tableRow;
    private final int rowIndex;

    Gherkin5Example(TableRow tableRow, int rowIndex) {
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
