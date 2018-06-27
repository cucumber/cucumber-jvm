package cucumber.runtime.filter;

import io.cucumber.messages.Messages.Pickle;

import java.util.List;
import java.util.regex.Pattern;

class NamePredicate implements PicklePredicate {
    private List<Pattern> patterns;

    NamePredicate(List<Pattern> patterns) {
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
