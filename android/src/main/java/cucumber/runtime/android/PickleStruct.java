package cucumber.runtime.android;

import gherkin.events.PickleEvent;

/**
 * Data class to hold the compiled pickles together with their language.
 */
public class PickleStruct {
    public final PickleEvent pickleEvent;
    public final String language;

    public PickleStruct(PickleEvent pickleEvent, String language) {
        this.pickleEvent = pickleEvent;
        this.language = language;
    }
}
