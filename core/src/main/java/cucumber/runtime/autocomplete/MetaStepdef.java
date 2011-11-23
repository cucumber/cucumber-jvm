package cucumber.runtime.autocomplete;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaStepdef {
    public String source;
    public String flags;
    public SortedSet<MetaStep> steps = new TreeSet<MetaStep>();
    private Pattern pattern;

    public boolean matches(String text) {
        Pattern p = pattern();
        Matcher m = p.matcher(text);
        return m.matches() || m.hitEnd();
    }

    private Pattern pattern() {
        if (pattern == null) {
            pattern = Pattern.compile(source);
        }
        return pattern;
    }

    public static class MetaStep implements Comparable<MetaStep> {
        public String name;
        public List<MetaArgument> args = new ArrayList<MetaArgument>();

        @Override
        public int compareTo(MetaStep other) {
            return name.compareTo(other.name);
        }
    }

    public static class MetaArgument {
        public int offset;
        public String val;
    }
}
