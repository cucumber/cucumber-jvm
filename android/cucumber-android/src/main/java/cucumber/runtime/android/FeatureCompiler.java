package cucumber.runtime.android;

import cucumber.runtime.filter.Filters;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to count scenarios, including outlined.
 */
final class FeatureCompiler {

    /**
     * Compilers the given {@code cucumberFeatures} to {@link PickleEvent}s.
     *
     * @param cucumberFeatures the list of {@link CucumberFeature} to compile
     * @return the compiled pickles in {@link PickleEvent}s
     */
    static List<PickleEvent> compile(final List<CucumberFeature> cucumberFeatures, final Filters filters) {
        List<PickleEvent> pickles = new ArrayList<PickleEvent>();
        cucumber.runtime.FeatureCompiler compiler = new cucumber.runtime.FeatureCompiler();
        for (final CucumberFeature feature : cucumberFeatures) {
            for (final PickleEvent pickleEvent : compiler.compileFeature(feature)) {
                if (filters.matchesFilters(pickleEvent)) {
                    pickles.add(pickleEvent);
                }
            }
        }
        return pickles;
    }

}
