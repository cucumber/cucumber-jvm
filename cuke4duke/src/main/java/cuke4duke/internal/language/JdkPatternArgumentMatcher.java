package cuke4duke.internal.language;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

public class JdkPatternArgumentMatcher {
    public static List<StepArgument> argumentsFrom(Pattern regexp, String stepName) {
        Matcher matcher = regexp.matcher(stepName);
        if(matcher.matches()) {
            List<StepArgument> groups = new ArrayList<StepArgument>();
            for(int i = 1; i <= matcher.groupCount(); i++) {
                groups.add(new StepArgument(matcher.group(i), matcher.start(i)));
            }
            return groups;
        } else {
            return null;
        }
    }
}
