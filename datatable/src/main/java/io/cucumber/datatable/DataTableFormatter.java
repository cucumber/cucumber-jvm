package io.cucumber.datatable;

import org.apiguardian.api.API;

import java.io.IOException;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@API(status = API.Status.STABLE)
public final class DataTableFormatter {

    private final Function<Integer, String> rowPrefix;
    private final boolean escapeDelimiters;

    private DataTableFormatter(Function<Integer, String> rowPrefix, boolean escapeDelimiters) {
        this.rowPrefix = rowPrefix;
        this.escapeDelimiters = escapeDelimiters;
    }

    public static DataTableFormatter.Builder builder() {
        return new Builder();
    }

    public String format(DataTable table) {
        StringBuilder result = new StringBuilder();
        formatTo(table, result);
        return result.toString();
    }

    public void formatTo(DataTable table, StringBuilder appendable) {
        try {
            formatTo(table, (Appendable) appendable);
        } catch (IOException e) {
            throw new CucumberDataTableException(e.getMessage(), e);
        }
    }

    public void formatTo(DataTable table, Appendable appendable) throws IOException {
        requireNonNull(table, "table may not be null");
        requireNonNull(appendable, "appendable may not be null");

        if (table.isEmpty()) {
            return;
        }
        // datatables are always square and non-sparse.
        int height = table.height();
        int width = table.width();

        // render the individual cells
        String[][] renderedCells = new String[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                renderedCells[i][j] = renderCell(table.cell(i, j));
            }
        }

        // find the longest rendered cell in each column
        int[] longestCellInColumnLength = new int[width];
        for (String[] row : renderedCells) {
            for (int colIndex = 0; colIndex < width; colIndex++) {
                int current = longestCellInColumnLength[colIndex];
                int candidate = row[colIndex].length();
                longestCellInColumnLength[colIndex] = Math.max(current, candidate);
            }
        }

        // Buffer the output with a StringBuilder, to avoid excess calls to
        // flush() from the NiceAppender used by PrettyFormatter.
        // Gradle adds a newline for every call to flush() unless the most
        // recent character written was already a newline. That leads
        // to some VERY difficult to read tables on the console.
        StringBuilder buffer = new StringBuilder();

        // print the rendered cells with padding
        for (int rowIndex = 0; rowIndex < height; rowIndex++) {
            printRowPrefix(buffer, rowIndex);
            buffer.append("| ");
            for (int colIndex = 0; colIndex < width; colIndex++) {
                String cellText = renderedCells[rowIndex][colIndex];
                buffer.append(cellText);
                int padding = longestCellInColumnLength[colIndex] - cellText.length();
                padSpace(buffer, padding);
                if (colIndex < width - 1) {
                    buffer.append(" | ");
                } else {
                    buffer.append(" |");
                }
            }
            buffer.append("\n");
        }
        appendable.append(buffer);
    }

    void printRowPrefix(Appendable buffer, int rowIndex) throws IOException {
        String prefix = rowPrefix.apply(rowIndex);
        if (prefix != null) {
            buffer.append(prefix);
        }
    }

    private String renderCell(String cell) {
        if (cell == null) {
            return "";
        }

        if (cell.isEmpty()) {
            return "[empty]";
        }

        if (!escapeDelimiters) {
            return cell;
        }

        return cell
                .replaceAll("\\\\(?!\\|)", "\\\\\\\\")
                .replaceAll("\\n", "\\\\n")
                .replaceAll("\\|", "\\\\|");
    }

    private void padSpace(Appendable buffer, int indent) throws IOException {
        for (int i = 0; i < indent; i++) {
            buffer.append(" ");
        }
    }

    public static final class Builder {
        private Function<Integer, String> rowPrefix = rowIndex -> "";
        private boolean escapeDelimiters = true;

        public Builder prefixRow(Function<Integer, String> rowPrefix) {
            requireNonNull(rowPrefix, "rowPrefix may not be null");
            this.rowPrefix = rowPrefix;
            return this;
        }

        public Builder prefixRow(String rowPrefix) {
            requireNonNull(rowPrefix, "rowPrefix may not be null");
            return prefixRow(rowIndex -> rowPrefix);
        }

        public Builder escapeDelimiters(boolean escapeDelimiters) {
            this.escapeDelimiters = escapeDelimiters;
            return this;
        }

        public DataTableFormatter build() {
            return new DataTableFormatter(rowPrefix, escapeDelimiters);
        }

    }

}
