package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;

import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Scenario;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

public class FeatureFilter {
    
    private final String tagPrefixLowerCase;
    
    public FeatureFilter(final String tagPrefix) {
        this.tagPrefixLowerCase = tagPrefix.toLowerCase();
    }
    
    public Map<String, Queue<CucumberFeature>> filterAndGroupBy(final List<CucumberFeature> cucumberFeatures) {
        final Map<String, Queue<CucumberFeature>> groupedFeatures = new LinkedHashMap<String, Queue<CucumberFeature>>();
        for(final CucumberFeature cucumberFeature : cucumberFeatures) {
            final GherkinDocument gherkinFeature = cucumberFeature.getGherkinFeature();
            final Feature feature = gherkinFeature.getFeature();
            final String tag = getMatchingTagFromFeature(feature);
            if (tag != null) {
                Queue<CucumberFeature> tagFeatures = groupedFeatures.get(tag);
                if (tagFeatures == null) {
                    tagFeatures = new LinkedList<CucumberFeature>();
                    groupedFeatures.put(tag, tagFeatures);
                }
                tagFeatures.add(cucumberFeature);
            }
        }
        return groupedFeatures;
        
    }

    private String getMatchingTagFromFeature(final Feature feature) {
        String tag = getMatchingTag(feature.getTags());
        if (tag == null) {
            tag = getMatchingTagFromScenarios(feature.getChildren());
        }
        return tag;
    }

    private String getMatchingTagFromScenarios(final List<ScenarioDefinition> scenarioDefinitions) {
        for (ScenarioDefinition scenarioDefinition : scenarioDefinitions) {
            final String tag = getMatchingTag(scenarioDefinition);
            if (tag != null) {
                return tag;
            }                
        }
        return null;
    }
    
    private String getMatchingTag(final ScenarioDefinition scenarioDefinition) {
        if (scenarioDefinition instanceof Scenario) {
            return getMatchingTag(((Scenario)scenarioDefinition).getTags());
        }
        if (scenarioDefinition instanceof ScenarioOutline) {
            return getMatchingTag(((ScenarioOutline)scenarioDefinition).getTags());
        }
        return null;
    }

    private String getMatchingTag(final List<Tag> tags) {
        for(final Tag tag : tags) {
            final String tagNameLowerCase = tag.getName().toLowerCase();
            if (tagNameLowerCase.startsWith(tagPrefixLowerCase)) {
                return tagNameLowerCase;
            }
        }
        return null;
    }
}
