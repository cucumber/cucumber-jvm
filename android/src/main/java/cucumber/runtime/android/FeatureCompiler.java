package cucumber.runtime.android;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to count scenarios, including outlined.
 */
public class FeatureCompiler {

    /**
     * Compilers the given {@code cucumberFeatures} to {@link PickleEvent}s.
     *
     * @param cucumberFeatures the list of {@link CucumberFeature} to compile
     * @return the compiled pickles in {@link PickleEvent}s
     */
    public static List<PickleEvent> compile(final List<CucumberFeature> cucumberFeatures, final Runtime runtime) {
        List<PickleEvent> pickles = new ArrayList<PickleEvent>();
        for (final CucumberFeature feature : cucumberFeatures) {
            for (final PickleEvent pickleEvent : runtime.compileFeature(feature)) {
                if (runtime.matchesFilters(pickleEvent)) {
                    pickles.add(pickleEvent);
                }
            }
        }
        return pickles;
    }

}
