package cucumber.runner;

import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Test;

import java.util.Collections;
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
        return createScenarioWithFeatureFileUriAndScenarioLocations(uri, asList(new PickleLocation(1, 1)));
    }

    private Scenario createScenarioWithScenarioLocations(List<PickleLocation> locations) {
        return createScenarioWithFeatureFileUriAndScenarioLocations("uri", locations);
    }

    private Scenario createScenarioWithFeatureFileUriAndScenarioLocations(String uri, List<PickleLocation> locations) {
        return new Scenario(mock(EventBus.class), new TestCase(
            Collections.<PickleStepTestStep>emptyList(),
            Collections.<HookTestStep>emptyList(),
            Collections.<HookTestStep>emptyList(),
            new PickleEvent(uri, new Pickle(
                "name",
                "en",
                Collections.<PickleStep>emptyList(),
                Collections.<PickleTag>emptyList(),
                locations
            )),
            false
        ));
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
