package cucumber.runtime.android;

import cucumber.runtime.filter.Filters;
import cucumber.runtime.model.CucumberFeature;
import io.cucumber.messages.Messages.Pickle;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to count scenarios, including outlined.
 */
final class FeatureCompiler {

    /**
     * Compilers the given {@code cucumberFeatures} to {@link Pickle}s.
     *
     * @param cucumberFeatures the list of {@link CucumberFeature} to compile
     * @return the compiled pickles in {@link Pickle}s
     */
    static List<Pickle> compile(final List<CucumberFeature> cucumberFeatures, final Filters filters) {
        List<Pickle> pickles = new ArrayList<Pickle>();
        for (final CucumberFeature feature : cucumberFeatures) {
            for (final Pickle pickle : feature.getPickles()) {
                if (filters.matchesFilters(pickle)) {
                    pickles.add(pickle);
                }
            }
        }
        return pickles;
    }

}
