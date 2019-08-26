package cucumber.runtime.autocomplete;

import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaStepdef {
    private static final Gson GSON = new GsonBuilder().create();

    public final SortedSet<MetaStep> steps = new TreeSet<MetaStep>();
    public String source;
    public String flags;
    private transient Pattern pattern;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetaStepdef that = (MetaStepdef) o;

        if (!flags.equals(that.flags)) return false;
        if (!source.equals(that.source)) return false;
        if (!steps.equals(that.steps)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = steps.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + flags.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    public static class MetaStep implements Comparable<MetaStep> {
        public String name;
        public final List<MetaArgument> args = new ArrayList<MetaArgument>();

        @Override
        public int compareTo(MetaStep other) {
            return name.compareTo(other.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MetaStep metaStep = (MetaStep) o;

            if (!args.equals(metaStep.args)) return false;
            if (!name.equals(metaStep.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + args.hashCode();
            return result;
        }
    }

    public static class MetaArgument {
        public int offset;
        public String val;
    }
}
