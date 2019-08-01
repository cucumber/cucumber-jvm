package io.cucumber.core.stepexpression;

import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleTable;

import java.util.ArrayList;
import java.util.List;

final class PickleTableConverter {

    private PickleTableConverter() {

    }

    static List<List<String>> toTable(PickleTable pickleTable) {
        List<List<String>> table = new ArrayList<>();
        for (PickleRow pickleRow : pickleTable.getRows()) {
            List<String> row = new ArrayList<>();
            for (PickleCell pickleCell : pickleRow.getCells()) {
                String value = pickleCell.getValue();
                row.add(value.isEmpty() ? null : value);
            }
            table.add(row);
        }
        return table;
    }
}
