package cucumber.runtime.android;

import gherkin.pickles.Pickle;

/**
 * Data class to hold the compiled pickles together with their language.
 */
public class PickleStruct {
    public final Pickle pickle;
    public final String language;

    public PickleStruct(Pickle pickle, String language) {
        this.pickle = pickle;
        this.language = language;
    }
}
