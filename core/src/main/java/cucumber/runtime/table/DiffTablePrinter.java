package cucumber.runtime.table;

import cucumber.runtime.table.DataTableDiff.DiffType;

import java.util.List;

public class DiffTablePrinter extends TablePrinter {
    private final List<DiffType> diffTypes;

    public DiffTablePrinter(List<DiffType> diffTypes) {
        this.diffTypes = diffTypes;
    }

    @Override
    protected void printStartIndent(StringBuilder buffer, int rowIndex) {
        switch (diffTypes.get(rowIndex)) {
        case NONE:
            buffer.append("      ");
            break;
        case DELETE:
            buffer.append("    - ");
            break;
        case INSERT:
            buffer.append("    + ");
            break;
        }
    }

}
