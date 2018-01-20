package cucumber.api;

import cucumber.runner.EventBus;
import cucumber.runtime.ScenarioImpl;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScenarioTest {

    @Test
    public void provides_the_uri_of_the_feature_file() {
        Scenario scenario = createScenarioWithFeatureFileUri(uri("path/file.feature"));

        assertEquals("path/file.feature", scenario.getUri());
    }

    @Test
    public void provides_the_scenario_line() {
        List<PickleLocation> scenarioLocation = asList(new PickleLocation(line(3), column(2)));
        Scenario scenario = createScenarioWithScenarioLocations(scenarioLocation);

        assertEquals(asList(3), scenario.getLines());
    }

    @Test
    public void provides_both_the_example_row_line_and_scenario_outline_line_for_scenarios_from_scenario_outlines() {
        List<PickleLocation> scenarioLocation = asList(new PickleLocation(line(8), column(4)), new PickleLocation(line(3), column(2)));
        Scenario scenario = createScenarioWithScenarioLocations(scenarioLocation);

        assertEquals(asList(8, 3), scenario.getLines());
    }

    @Test
    public void provides_the_uri_and_scenario_line_as_unique_id() {
        List<PickleLocation> scenarioLocation = asList(new PickleLocation(line(3), column(2)));
        Scenario scenario = createScenarioWithFeatureFileUriAndScenarioLocations(uri("path/file.feature"), scenarioLocation);

        assertEquals("path/file.feature:3", scenario.getId());
    }

    @Test
    public void provides_the_uri_and_example_row_line_as_unique_id_for_scenarios_from_scenario_outlines() {
        List<PickleLocation> scenarioLocation = asList(new PickleLocation(line(8), column(4)), new PickleLocation(line(3), column(2)));
        Scenario scenario = createScenarioWithFeatureFileUriAndScenarioLocations(uri("path/file.feature"), scenarioLocation);

        assertEquals("path/file.feature:8", scenario.getId());
    }

    private Scenario createScenarioWithFeatureFileUri(String uri) {
        return new ScenarioImpl(mock(EventBus.class), new PickleEvent(uri, mockPickle()));
    }

    private Scenario createScenarioWithFeatureFileUriAndScenarioLocations(String uri, List<PickleLocation> locations) {
        return new ScenarioImpl(mock(EventBus.class), new PickleEvent(uri, mockPickle(locations)));
    }

    private Scenario createScenarioWithScenarioLocations(List<PickleLocation> locations) {
        return new ScenarioImpl(mock(EventBus.class), new PickleEvent("uri", mockPickle(locations)));
    }

    private Pickle mockPickle() {
        return mockPickle(asList(new PickleLocation(1, 1)));
    }

    private Pickle mockPickle(List<PickleLocation> locations) {
        Pickle pickle = mock(Pickle.class);
        when(pickle.getLocations()).thenReturn(locations);
        return pickle;
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
