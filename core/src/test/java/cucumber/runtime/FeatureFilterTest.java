package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.Background;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Scenario;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Tag;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class FeatureFilterTest {
    
    private static final String PREFIX = "@synchronized";
    
    private FeatureFilter filter = new FeatureFilter(PREFIX);

    @Mock
    private CucumberFeature featureWithNoTags;
    
    @Mock
    private CucumberFeature featureNoMatchingTag;

    @Mock
    private CucumberFeature featureWithMatchGroup1;

    @Mock
    private CucumberFeature feature2WithMatchGroup1;

    @Mock
    private CucumberFeature featureWithMatchGroup2;

    @Mock
    private CucumberFeature featureWithNoMatchingScenario;
    
    @Mock
    private CucumberFeature featureWithMatchScenarioGroup1;

    @Mock
    private CucumberFeature featureWithMatchScenarioOutlineGroup2;
    
    @Mock
    private Tag nonMatchingTag;
    
    @Mock
    private Tag matchingTagGroup;

    @Mock
    private Tag matchingTagGroup2;
        
    @Mock
    private Background background;

    @Mock
    private Scenario nonMatchScenario;
    
    @Mock
    private ScenarioOutline nonMatchScenarioOutline;
    
    @Mock
    private Scenario matchScenarioGroup1;
    
    @Mock
    private ScenarioOutline matchScenarioOutlineGroup2;
    
    private List<CucumberFeature> featuresToFilter = new ArrayList<CucumberFeature>();
    
    private List<CucumberFeature> expectedExcludedFeatures;
    
    @Before
    public void setUp() {
        expectedExcludedFeatures = Arrays.asList(featureWithNoTags, featureNoMatchingTag, featureWithNoMatchingScenario);
        
        featuresToFilter.addAll(expectedExcludedFeatures);
        featuresToFilter.add(featureWithMatchGroup1);
        featuresToFilter.add(feature2WithMatchGroup1);
        featuresToFilter.add(featureWithMatchGroup2);
        featuresToFilter.add(featureWithMatchScenarioGroup1);
        featuresToFilter.add(featureWithMatchScenarioOutlineGroup2);

        mockFeatureTags(featureWithNoTags);
        mockFeatureTags(featureNoMatchingTag, nonMatchingTag);        
        mockFeatureTags(featureWithMatchGroup1, nonMatchingTag, matchingTagGroup);
        mockFeatureTags(feature2WithMatchGroup1, matchingTagGroup);
        mockFeatureTags(featureWithMatchGroup2, matchingTagGroup2);
        
        mockScenarioTags(background);
        mockScenarioTags(nonMatchScenario, nonMatchingTag);
        mockScenarioTags(matchScenarioGroup1, matchingTagGroup);
        mockScenarioTags(matchScenarioOutlineGroup2, matchingTagGroup2);

        mockFeatureChildren(featureWithNoMatchingScenario, background, nonMatchScenario, nonMatchScenarioOutline);
        mockFeatureChildren(featureWithMatchScenarioGroup1, background, matchScenarioGroup1, nonMatchScenarioOutline);
        mockFeatureChildren(featureWithMatchScenarioOutlineGroup2, background, nonMatchScenario, matchScenarioOutlineGroup2);
        
        given(nonMatchingTag.getName()).willReturn("@another-tag");
        given(matchingTagGroup.getName()).willReturn(PREFIX);
        given(matchingTagGroup2.getName()).willReturn(PREFIX + "-2");
    }
    
    private void mockFeatureTags(final CucumberFeature cucumberFeature, final Tag... tags) {
        final GherkinDocument gherkinDocument = mock(GherkinDocument.class);
        given(cucumberFeature.getGherkinFeature()).willReturn(gherkinDocument);
        final Feature feature = mock(Feature.class);
        given(gherkinDocument.getFeature()).willReturn(feature);
        given(feature.getTags()).willReturn(Arrays.asList(tags));
    }

    private void mockScenarioTags(final ScenarioDefinition feature, final Tag... tags) {
        if (feature instanceof Scenario) {
            given(((Scenario) feature).getTags()).willReturn(Arrays.asList(tags));
        } else if (feature instanceof ScenarioOutline) {
            given(((ScenarioOutline) feature).getTags()).willReturn(Arrays.asList(tags));
        }
    }

    private void mockFeatureChildren(final CucumberFeature cucumberFeature, final ScenarioDefinition... scenarios) {
        mockFeatureTags(cucumberFeature);
        final Feature feature = cucumberFeature.getGherkinFeature().getFeature();
        given(feature.getChildren()).willReturn(Arrays.asList(scenarios));
    }
    
    @Test
    public void should_not_return_empty_features_when_no_match_found() {
        final Map<String, Queue<CucumberFeature>> result = filter.filterAndGroupBy(Collections.singletonList(featureNoMatchingTag));

        assertTrue("result should be empty", result.isEmpty());
    }
    
    @Test
    public void should_not_return_features_which_do_not_match_filter() {
        final Map<String, Queue<CucumberFeature>> result = filter.filterAndGroupBy(featuresToFilter);
        
        assertFalse("result shouldn't be empty", result.isEmpty());
        for(Map.Entry<String, Queue<CucumberFeature>> entry : result.entrySet()) {
            for(final CucumberFeature notExpected : expectedExcludedFeatures) {
                assertFalse("Feature [" + notExpected + "] shouldn't have been included in any resulting List", entry.getValue().contains(notExpected));
            }
        }
    }
    
    @Test
    public void should_return_features_starting_with_filter_grouped_by_actual_tag() {
        final Map<String, Queue<CucumberFeature>> expected = new LinkedHashMap<String, Queue<CucumberFeature>>();
        expected.put(matchingTagGroup.getName(), new LinkedList<CucumberFeature>(Arrays.asList(featureWithMatchGroup1, feature2WithMatchGroup1, featureWithMatchScenarioGroup1)));
        expected.put(matchingTagGroup2.getName(), new LinkedList<CucumberFeature>(Arrays.asList(featureWithMatchGroup2, featureWithMatchScenarioOutlineGroup2)));
                
        final Map<String, Queue<CucumberFeature>> result = filter.filterAndGroupBy(featuresToFilter);

        assertEquals("group was not expected", expected, result);
    }

    @Test
    public void should_ignore_casing_when_matching_tags() {
        given(matchingTagGroup.getName()).willReturn(PREFIX.toUpperCase());

        final Map<String, Queue<CucumberFeature>> expected = new LinkedHashMap<String, Queue<CucumberFeature>>();
        expected.put(matchingTagGroup.getName().toLowerCase(), new LinkedList<CucumberFeature>(Arrays.asList(featureWithMatchGroup1, feature2WithMatchGroup1, featureWithMatchScenarioGroup1)));
        expected.put(matchingTagGroup2.getName(), new LinkedList<CucumberFeature>(Arrays.asList(featureWithMatchGroup2, featureWithMatchScenarioOutlineGroup2)));

        final Map<String, Queue<CucumberFeature>> result = filter.filterAndGroupBy(featuresToFilter);

        assertEquals("group was not expected", expected, result);
    }

    @Test
    public void should_ignore_casing_when_matching_tags_constructor() {
        final FeatureFilter localFilter = new FeatureFilter(PREFIX.toUpperCase());

        final Map<String, Queue<CucumberFeature>> expected = new LinkedHashMap<String, Queue<CucumberFeature>>();
        expected.put(matchingTagGroup.getName(), new LinkedList<CucumberFeature>(Arrays.asList(featureWithMatchGroup1, feature2WithMatchGroup1, featureWithMatchScenarioGroup1)));
        expected.put(matchingTagGroup2.getName(), new LinkedList<CucumberFeature>(Arrays.asList(featureWithMatchGroup2, featureWithMatchScenarioOutlineGroup2)));

        final Map<String, Queue<CucumberFeature>> result = localFilter.filterAndGroupBy(featuresToFilter);

        assertEquals("group was not expected", expected, result);
    }
}