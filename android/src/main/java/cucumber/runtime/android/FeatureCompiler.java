package cucumber.runtime.android;

import cucumber.runtime.Runtime;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to count scenarios, including outlined.
 */
public class FeatureCompiler {

    /**
     * Compilers the given {@code cucumberFeatures} to {@link Pickle}s.
     *
     * @param cucumberFeatures the list of {@link CucumberFeature} to compile
     * @return the compiled pickles in {@link PickleStruct}s
     */
    public static List<PickleStruct> compile(final List<CucumberFeature> cucumberFeatures, final Runtime runtime) {
        List<PickleStruct> pickles = new ArrayList<PickleStruct>();
        Compiler compiler = new Compiler();
        for (final CucumberFeature feature : cucumberFeatures) {
            for (final Pickle pickle : compiler.compile(feature.getGherkinFeature())) {
                final PickleEvent pickleEvent = new PickleEvent(feature.getPath(), pickle);
                if (runtime.matchesFilters(pickleEvent)) {
                    pickles.add(new PickleStruct(pickleEvent, feature.getLanguage()));
                }
            }
        }
        return pickles;
    }

}
