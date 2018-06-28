package cucumber.runtime.filter;

import gherkin.events.PickleEvent;

import java.util.List;
import java.util.regex.Pattern;

class NamePredicate implements PicklePredicate {
    private List<Pattern> patterns;

    NamePredicate(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public boolean apply(PickleEvent pickleEvent) {
        String name = pickleEvent.pickle.getName();
        for (Pattern pattern : patterns) {
            if (pattern.matcher(name).find()) {
                return true;
            }
        }
        return false;
    }

}
