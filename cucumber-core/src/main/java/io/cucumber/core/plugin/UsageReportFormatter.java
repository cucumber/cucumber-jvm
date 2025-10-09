package io.cucumber.core.plugin;

import io.cucumber.core.plugin.UsageReport.Statistics;
import io.cucumber.core.plugin.UsageReport.StepDefinitionUsage;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

final class UsageReportFormatter {

    public static final int MAX_NUMBER_OF_STEPS = 5;

    public static String format(UsageReport usageReport) {

        List<List<String>> table = new ArrayList<>();
        table.add(Arrays.asList("Expression/Text", "Duration", "Mean", "±", "Error", "Location"));

        usageReport.getStepDefinitions()
                .stream()
                .sorted(comparing(StepDefinitionUsage::getDuration, nullsFirst(comparing(Statistics::getMean))).reversed())
                .forEach(stepDefinitionUsage -> {
                    if (stepDefinitionUsage.getSteps().isEmpty()) {
                        table.add(Arrays.asList(
                                "  UNUSED",
                                "",
                                "",
                                "",
                                "",
                                ""
                        ));
                    } else {
                        table.add(Arrays.asList(
                                stepDefinitionUsage.getExpression(),
                                formatDuration(stepDefinitionUsage.getDuration().getSum()),
                                formatDuration(stepDefinitionUsage.getDuration().getMean()),
                                "±",
                                formatDuration(stepDefinitionUsage.getDuration().getMoe95()),
                                stepDefinitionUsage.getLocation()
                        ));
                        stepDefinitionUsage.getSteps().stream()
                                .sorted(comparing(UsageReport.StepUsage::getDuration).reversed())
                                .limit(5)
                                .forEach(stepUsage -> {

                                    table.add(Arrays.asList(
                                            "  " + stepUsage.getText(),
                                            formatDuration(stepUsage.getDuration()),
                                            "",
                                            "",
                                            "",
                                            stepUsage.getLocation()
                                    ));

                                });
                        if (stepDefinitionUsage.getSteps().size() > MAX_NUMBER_OF_STEPS) {
                            table.add(Arrays.asList(
                                    "  " + (stepDefinitionUsage.getSteps().size() - 5) + " more",
                                    "",
                                    "",
                                    "",
                                    "",
                                    ""
                            ));
                        }
                    }

                });

        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        int[] longestCellLengthInColumn = findLongestCellLengthInColumn(table);
        for (List<String> row : table) {
            StringJoiner rowJoiner = new StringJoiner(" ");
            for (int j = 0; j < row.size(); j++) {
                // TODO: Allignment
                String cell = row.get(j);
                int padding = longestCellLengthInColumn[j] - cell.length();
                String newElement = renderCellWithPadding(cell, padding);
                rowJoiner.add(newElement);
            }
            joiner.add(rowJoiner.toString());
        }
        return joiner.toString();
    }

    private static String formatDuration(Duration duration) {
        return toBigDecimalSeconds(duration).setScale(3, HALF_EVEN).toPlainString();
    }

    private static BigDecimal toBigDecimalSeconds(Duration duration) {
        return BigDecimal.valueOf(duration.getSeconds()).add(BigDecimal.valueOf(duration.getNano(), 9));
    }

    private static Duration getMean(StepDefinitionUsage stepDefinitionUsage) {
        return stepDefinitionUsage.getDuration().getMean();
    }

    private static int[] findLongestCellLengthInColumn(List<List<String>> renderedCells) {
        // datatables are always square and non-sparse.
        int width = renderedCells.get(0).size();
        int[] longestCellInColumnLength = new int[width];
        for (List<String> row : renderedCells) {
            for (int colIndex = 0; colIndex < width; colIndex++) {
                int current = longestCellInColumnLength[colIndex];
                int candidate = row.get(colIndex).length();
                longestCellInColumnLength[colIndex] = Math.max(current, candidate);
            }
        }
        return longestCellInColumnLength;
    }

    private static String renderCellWithPadding(String cellText, int padding) {
        StringBuilder result = new StringBuilder();
        result.append(cellText);
        padSpace(result, padding);
        return result.toString();
    }

    private static void padSpace(StringBuilder result, int padding) {
        for (int i = 0; i < padding; i++) {
            result.append(" ");
        }
    }

}
