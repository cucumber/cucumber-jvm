package cuke4duke.internal.language;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

public class JdkRegexpGroup implements Group {
    private final String val;
    private final int start;

    public JdkRegexpGroup(String val, int start) {
        this.val = val;
        this.start = start;
    }

    public String getVal() {
        return val;
    }

    public int getStart() {
        return start;
    }

    public static List<Group> groupsFrom(Pattern regexp, String stepName) {
        Matcher matcher = regexp.matcher(stepName);
        if(matcher.matches()) {
            List<Group> groups = new ArrayList<Group>();
            for(int i = 1; i <= matcher.groupCount(); i++) {
                groups.add(new JdkRegexpGroup(matcher.group(i), matcher.start(i)));
            }
            return groups;
        } else {
            return null;
        }
    }
}
