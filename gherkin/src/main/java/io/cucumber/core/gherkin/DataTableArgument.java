package io.cucumber.core.gherkin;

import java.util.List;

public interface DataTableArgument extends Argument, io.cucumber.plugin.event.DataTableArgument {

    @Override
    List<List<String>> cells();

    @Override
    int getLine();

}
