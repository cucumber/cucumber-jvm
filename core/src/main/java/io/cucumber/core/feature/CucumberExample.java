package io.cucumber.core.feature;

import gherkin.ast.TableRow;

public final class CucumberExample implements Located, Named {

    private final TableRow tableRow;
    private final int rowIndex;

    CucumberExample(TableRow tableRow, int rowIndex) {
        this.tableRow = tableRow;
        this.rowIndex = rowIndex;
    }

    @Override
    public String getKeyWord() {
        return null;
    }

    public String getName() {
        return "Example #" + rowIndex;
    }

    @Override
    public CucumberLocation getLocation() {
        return CucumberLocation.from(tableRow.getLocation());
    }
}
