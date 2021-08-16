package io.cucumber.core.gherkin.messages;

import io.cucumber.messages.types.TableRow;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.util.Optional;

final class GherkinMessagesExample implements Node.Example {

    private final TableRow tableRow;
    private final int rowIndex;
    private final Node parent;

    GherkinMessagesExample(Node parent, TableRow tableRow, int rowIndex) {
        this.parent = parent;
        this.tableRow = tableRow;
        this.rowIndex = rowIndex;
    }

    @Override
    public Location getLocation() {
        return GherkinMessagesLocation.from(tableRow.getLocation());
    }

    @Override
    public Optional<String> getKeyword() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getName() {
        return Optional.of("Example #" + rowIndex);
    }

    @Override
    public Optional<Node> getParent() {
        return Optional.of(parent);
    }

}
