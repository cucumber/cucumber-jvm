package cucumber.java.utils;

import java.util.ArrayList;
import java.util.List;

public class RegexMatch {
    protected boolean regexMatched;
    protected List<RegexSubmatch> submatches = new ArrayList<RegexSubmatch>();

    public boolean matches() {
       return regexMatched;
    }

    public List<RegexSubmatch> getSubmatches() {
        return submatches;
    }
}
