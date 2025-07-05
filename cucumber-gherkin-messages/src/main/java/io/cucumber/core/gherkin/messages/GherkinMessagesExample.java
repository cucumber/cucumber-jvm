package io.cucumber.core.gherkin.messages;

import io.cucumber.messages.types.TableRow;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.net.URI;
import java.util.Optional;

final class GherkinMessagesExample implements Node.Example {

    private final TableRow tableRow;
    private final int examplesIndex;
    private final int rowIndex;
    private final Node parent;

    GherkinMessagesExample(Node parent, TableRow tableRow, int examplesIndex, int rowIndex) {
        this.parent = parent;
        this.tableRow = tableRow;
        this.examplesIndex = examplesIndex;
        this.rowIndex = rowIndex;
    }

    @Override
    public URI getUri() {
        return parent.getUri();
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
        return Optional.of("Example #" + examplesIndex + "." + rowIndex);
    }

    @Override
    public Optional<Node> getParent() {
        return Optional.of(parent);
    }

}
