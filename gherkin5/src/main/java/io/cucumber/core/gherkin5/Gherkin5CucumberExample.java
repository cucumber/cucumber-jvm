package io.cucumber.core.gherkin5;

import gherkin.ast.TableRow;
import io.cucumber.core.gherkin.CucumberExample;
import io.cucumber.core.gherkin.CucumberLocation;

import static io.cucumber.core.gherkin5.Gherkin5CucumberLocation.from;

public final class Gherkin5CucumberExample implements CucumberExample {

    private final TableRow tableRow;
    private final int rowIndex;

    Gherkin5CucumberExample(TableRow tableRow, int rowIndex) {
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
    public CucumberLocation getLocation() {
        return from(tableRow.getLocation());
    }
}
