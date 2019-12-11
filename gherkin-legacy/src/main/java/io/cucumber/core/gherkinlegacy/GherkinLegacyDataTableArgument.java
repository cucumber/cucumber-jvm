package io.cucumber.core.gherkinlegacy;

import gherkin.pickles.PickleTable;
import io.cucumber.core.gherkin.DataTableArgument;

import java.util.AbstractList;
import java.util.List;

final class GherkinLegacyDataTableArgument implements DataTableArgument {

    private final CellView cells;
    private final int line;

    GherkinLegacyDataTableArgument(PickleTable table) {
        this.cells = new CellView(table);
        this.line = table.getLocation().getLine();
    }

    @Override
    public List<List<String>> cells() {
        return cells;
    }

    @Override
    public int getLine() {
        return line;
    }

    private static class CellView extends AbstractList<List<String>> {
        private final PickleTable table;

        CellView(PickleTable table) {
            this.table = table;
        }

        @Override
        public List<String> get(int row) {
            return new AbstractList<String>() {
                @Override
                public String get(int column) {
                    return table.getRows().get(row).getCells().get(column).getValue();
                }

                @Override
                public int size() {
                    return table.getRows().get(row).getCells().size();
                }
            };
        }

        @Override
        public int size() {
            return table.getRows().size();
        }
    }
}
