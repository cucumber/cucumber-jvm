package cucumber.runtime;

import gherkin.pickles.Pickle;

import java.util.List;
import java.util.regex.Pattern;

public class NamePredicate implements PicklePredicate {
    private List<Pattern> patterns;

    public NamePredicate(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public boolean apply(Pickle pickle) {
        String name = pickle.getName();
        for (Pattern pattern : patterns) {
            if (pattern.matcher(name).find()) {
                return true;
            }
        }
        return false;
    }

}
