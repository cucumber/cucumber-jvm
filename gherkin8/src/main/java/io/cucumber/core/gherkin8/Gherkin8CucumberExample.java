package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.CucumberExample;
import io.cucumber.core.gherkin.CucumberLocation;
import io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow;

final class Gherkin8CucumberExample implements CucumberExample {

    private final TableRow tableRow;
    private final int rowIndex;

    Gherkin8CucumberExample(TableRow tableRow, int rowIndex) {
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
    public CucumberLocation getLocation() {
        return Gherkin8CucumberLocation.from(tableRow.getLocation());
    }
}
