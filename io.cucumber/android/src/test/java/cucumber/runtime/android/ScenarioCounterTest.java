package cucumber.runtime.android;

import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenario;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.ExamplesTableRow;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScenarioCounterTest {

    @Test
    public void calculates_number_of_tests_for_regular_scenarios() {

        // given
        final List<CucumberFeature> cucumberFeatures = createCucumberFeaturesWithScenarios(1, 2);

        // when
        final int result = ScenarioCounter.countScenarios(cucumberFeatures);

        // then
        assertThat(result, is(2));
    }

    @Test
    public void calculates_number_of_tests_for_scenarios_with_examples() {

        // given 2 scenario outlines with 2 examples each and 2 rows (excluding the header row) each
        final List<CucumberFeature> cucumberFeatures = createCucumberFeaturesWithScenarioOutlines(1, 2, 2, 2);

        // when
        final int result = ScenarioCounter.countScenarios(cucumberFeatures);

        // then
        assertThat(result, is(8));
    }

    private List<CucumberFeature> createCucumberFeaturesWithScenarios(
            final int numberOfCucumberFeatures,
            final int numberOfCucumberScenarios) {

        final List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();

        for (int f = 0; f < numberOfCucumberFeatures; f++) {

            final CucumberFeature cucumberFeature = mock(CucumberFeature.class);
            cucumberFeatures.add(cucumberFeature);

            final List<CucumberTagStatement> cucumberTagStatements = new ArrayList<CucumberTagStatement>();
            for (int s = 0; s < numberOfCucumberScenarios; s++) {
                cucumberTagStatements.add(mock(CucumberScenario.class));
            }

            when(cucumberFeature.getFeatureElements()).thenReturn(cucumberTagStatements);
        }
        return cucumberFeatures;
    }

    private List<CucumberFeature> createCucumberFeaturesWithScenarioOutlines(
            final int numberOfCucumberFeatures,
            final int numberOfScenarioOutlines,
            final int numberOfCucumberExamples,
            final int numberOfExampleRows) {

        final int numberOfExampleRowsIncludingHeaderRow = numberOfExampleRows + 1;
        final List<CucumberFeature> cucumberFeatures = new ArrayList<CucumberFeature>();

        for (int f = 0; f < numberOfCucumberFeatures; f++) {

            final CucumberFeature cucumberFeature = mock(CucumberFeature.class);
            cucumberFeatures.add(cucumberFeature);

            // set up 2 scenarios outlines
            final List<CucumberTagStatement> cucumberTagStatements = new ArrayList<CucumberTagStatement>();

            for (int o = 0; o < numberOfScenarioOutlines; o++) {
                cucumberTagStatements.add(mock(CucumberScenarioOutline.class));
            }
            when(cucumberFeature.getFeatureElements()).thenReturn(cucumberTagStatements);

            // with 2 examples for each scenario outline
            for (final CucumberTagStatement cucumberTagStatement : cucumberTagStatements) {
                final CucumberScenarioOutline cucumberScenarioOutline = (CucumberScenarioOutline) cucumberTagStatement;
                final List<CucumberExamples> cucumberExamplesList = createMockList(CucumberExamples.class, numberOfCucumberExamples);
                when(cucumberScenarioOutline.getCucumberExamplesList()).thenReturn(cucumberExamplesList);

                // each example should have two rows (excluding the header row)
                for (final CucumberExamples cucumberExamples : cucumberExamplesList) {

                    final Examples examples = mock(Examples.class);
                    when(examples.getRows()).thenReturn(createMockList(ExamplesTableRow.class, numberOfExampleRowsIncludingHeaderRow));
                    when(cucumberExamples.getExamples()).thenReturn(examples);

                }
            }

        }

        return cucumberFeatures;
    }

    private static <T> List<T> createMockList(final Class<T> type, final int numberOfMocks) {
        final List<T> list = new ArrayList<T>();

        for (int i = 0; i < numberOfMocks; i++) {
            list.add(mock(type));
        }
        return list;
    }
}
