package io.cucumber.stepexpression;

import io.cucumber.messages.Messages.PickleTable;
import io.cucumber.messages.Messages.PickleTableCell;
import io.cucumber.messages.Messages.PickleTableRow;

import java.util.ArrayList;
import java.util.List;

class PickleTableConverter {
    static List<List<String>> toTable(PickleTable pickleTable) {
        List<List<String>> table = new ArrayList<List<String>>();
        for (PickleTableRow pickleRow : pickleTable.getRowsList()) {
            List<String> row = new ArrayList<String>();
            for (PickleTableCell pickleCell : pickleRow.getCellsList()) {
                row.add(pickleCell.getValue());
            }
            table.add(row);
        }
        return table;
    }
}
