package cucumber.runtime.table;

import java.util.List;

public class TablePrinter {
    private int[][] cellLengths;
    private int[] maxLengths;

    public void printTable(List<List<String>> table, StringBuilder result) {
        calculateColumnAndMaxLengths(table);
        for (int i = 0; i < table.size(); ++i) {
            printRow(table.get(i), i, result);
            result.append("\n");
        }

    }

    protected void printStartIndent(StringBuilder buffer, int rowIndex) {
        buffer.append("      ");
    }

    private void calculateColumnAndMaxLengths(List<List<String>> rows) {
        // find the largest row
        int columnCount = 0;
        for (List<String> row : rows) {
            if (columnCount < row.size()) {
                columnCount = row.size();
            }
        }

        cellLengths = new int[rows.size()][columnCount];
        maxLengths = new int[columnCount];
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            final List<String> cells = rows.get(rowIndex);
            for (int colIndex = 0; colIndex < columnCount; colIndex++) {
                final String cell = getCellSafely(cells, colIndex);
                final int length = escapeCell(cell).length();
                cellLengths[rowIndex][colIndex] = length;
                maxLengths[colIndex] = Math.max(maxLengths[colIndex], length);
            }
        }
    }

    private String getCellSafely(final List<String> cells, final int colIndex) {
        return (colIndex < cells.size()) ? cells.get(colIndex) : "";
    }

    private void printRow(List<String> cells, int rowIndex, StringBuilder buffer) {
        printStartIndent(buffer, rowIndex);
        buffer.append("| ");
        for (int colIndex = 0; colIndex < maxLengths.length; colIndex++) {
            String cellText = escapeCell(getCellSafely(cells, colIndex));
            buffer.append(cellText);
            int padding = maxLengths[colIndex] - cellLengths[rowIndex][colIndex];
            padSpace(buffer, padding);
            if (colIndex < maxLengths.length - 1) {
                buffer.append(" | ");
            } else {
                buffer.append(" |");
            }
        }
    }

    private String escapeCell(String cell) {
        return cell.replaceAll("\\\\(?!\\|)", "\\\\\\\\").replaceAll("\\n", "\\\\n").replaceAll("\\|", "\\\\|");
    }

    private void padSpace(StringBuilder buffer, int indent) {
        for (int i = 0; i < indent; i++) {
            buffer.append(" ");
        }
    }
}
