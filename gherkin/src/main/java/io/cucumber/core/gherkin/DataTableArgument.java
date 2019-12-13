package io.cucumber.core.gherkin;

import java.util.List;

public interface DataTableArgument extends Argument, io.cucumber.plugin.event.DataTableArgument {
    List<List<String>> cells();

    int getLine();
}
