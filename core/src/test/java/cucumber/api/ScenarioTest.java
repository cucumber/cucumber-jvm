package cucumber.api;

import cucumber.messages.Sources.Location;
import cucumber.runner.EventBus;
import cucumber.runtime.ScenarioImpl;
import org.junit.Test;

import static cucumber.runtime.PickleHelper.location;
import static cucumber.runtime.PickleHelper.pickle;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ScenarioTest {

    @Test
    public void provides_the_uri_of_the_feature_file() {
        Scenario scenario = scenario(uri("path/file.feature"));

        assertEquals("path/file.feature", scenario.getUri());
    }

    @Test
    public void provides_the_scenario_line() {
        Scenario scenario = scenario(newLocation(3, 2));

        assertEquals(asList(3), scenario.getLines());
    }

    private Location newLocation(int line, int column) {
        return Location.newBuilder().setLine(line).setColumn(column).build();
    }

    @Test
    public void provides_both_the_example_row_line_and_scenario_outline_line_for_scenarios_from_scenario_outlines() {
        Scenario scenario = scenario(newLocation(line(8), column(4)), newLocation(line(3), column(2)));
        assertEquals(asList(8, 3), scenario.getLines());
    }

    @Test
    public void provides_the_uri_and_scenario_line_as_unique_id() {
        Scenario scenario = scenario(uri("path/file.feature"), newLocation(line(3), column(2)));

        assertEquals("path/file.feature:3", scenario.getId());
    }

    @Test
    public void provides_the_uri_and_example_row_line_as_unique_id_for_scenarios_from_scenario_outlines() {
        Scenario scenario = scenario(uri("path/file.feature"), newLocation(line(8), column(4)), newLocation(line(3), column(2)));

        assertEquals("path/file.feature:8", scenario.getId());
    }

    private Scenario scenario(String uri) {
        return new ScenarioImpl(mock(EventBus.class), pickle(uri, location()));
    }

    private Scenario scenario(String uri, Location... locations) {
        return new ScenarioImpl(mock(EventBus.class), pickle(uri, locations));
    }

    private Scenario scenario(Location... locations) {
        return new ScenarioImpl(mock(EventBus.class), pickle(locations));
    }

    private String uri(String uri) {
        return uri;
    }

    private int line(int line) {
        return line;
    }

    private int column(int column) {
        return column;
    }
}
