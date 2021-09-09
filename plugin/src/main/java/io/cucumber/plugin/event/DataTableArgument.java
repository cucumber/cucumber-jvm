package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.util.List;

/**
 * Represents a Gherkin data table argument.
 */
@API(status = API.Status.STABLE)
public interface DataTableArgument extends StepArgument {

    List<List<String>> cells();

    int getLine();

}
