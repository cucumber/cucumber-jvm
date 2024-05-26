package io.cucumber.core.gherkin.messages;

import io.cucumber.messages.types.TableRow;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Node;

import java.util.Optional;

final class GherkinMessagesExample implements Node.Example {

    private final GherkinMessagesFeature feature;
    private final TableRow tableRow;
    private final int examplesIndex;
    private final int rowIndex;
    private final Node parent;

    GherkinMessagesExample(
            GherkinMessagesFeature feature, Node parent, TableRow tableRow, int examplesIndex, int rowIndex
    ) {
        this.feature = feature;
        this.parent = parent;
        this.tableRow = tableRow;
        this.examplesIndex = examplesIndex;
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
        String pickleName = feature.getPickleAt(this).getName();
        boolean parameterized = !parent.getParent()
                .filter(GherkinMessagesScenarioOutline.class::isInstance)
                .flatMap(Node::getName)
                .map(pickleName::equals)
                .orElse(true);

        StringBuilder builder = new StringBuilder("Example #" + examplesIndex + "." + rowIndex);
        if (parameterized) {
            builder.append(" - ").append(pickleName);
        }
        return Optional.of(builder.toString());
    }

    @Override
    public Optional<Node> getParent() {
        return Optional.of(parent);
    }

}
