package cucumber.runtime;

import gherkin.pickles.Pickle;
import gherkin.pickles.PickleTag;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class TagPredicate implements PicklePredicate {
    private TagExpression tagExpression;

    public TagPredicate(List<String> tags) {
        this.tagExpression = new TagExpression(tags);
    }

    @Override
    public boolean apply(Pickle pickle) {
        List<PickleTag> tags;
        try { // TODO: Fix when Gherkin provide a getter for the tags.
            Field f;
            f = pickle.getClass().getDeclaredField("tags");
            f.setAccessible(true);
            tags = (List<PickleTag>) f.get(pickle);
        } catch (Exception e) {
            tags = Collections.<PickleTag>emptyList();
        }
        if (tagExpression.evaluate(tags)) {
            return true;
        }
        return false;
    }

}
