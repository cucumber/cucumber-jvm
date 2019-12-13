package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Location;
import io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow;

final class GherkinMessagesExample implements Example {

    private final TableRow tableRow;
    private final int rowIndex;

    GherkinMessagesExample(TableRow tableRow, int rowIndex) {
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
        return GherkinMessagesLocation.from(tableRow.getLocation());
    }
}
