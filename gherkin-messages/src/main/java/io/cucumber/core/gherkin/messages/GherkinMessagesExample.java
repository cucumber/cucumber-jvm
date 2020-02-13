package io.cucumber.core.gherkin.messages;

import io.cucumber.plugin.event.Location;
import io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow;
import io.cucumber.plugin.event.Node;

final class GherkinMessagesExample implements Node.Example {

    private final TableRow tableRow;
    private final int rowIndex;

    GherkinMessagesExample(TableRow tableRow, int rowIndex) {
        this.tableRow = tableRow;
        this.rowIndex = rowIndex;
    }

    @Override
    public String getKeyword() {
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
