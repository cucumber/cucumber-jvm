package cucumber.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shellwords {
    private static final Pattern SHELLWORDS_PATTERN = Pattern.compile("[^\\s']+|'([^']*)'");

    private Shellwords() {
    }

    public static List<String> parse(String cmdline) {
        List<String> matchList = new ArrayList<String>();
        Matcher shellwordsMatcher = SHELLWORDS_PATTERN.matcher(cmdline);
        while (shellwordsMatcher.find()) {
            if (shellwordsMatcher.group(1) != null) {
                matchList.add(shellwordsMatcher.group(1));
            } else {
                matchList.add(shellwordsMatcher.group());
            }
        }
        return matchList;
    }
}
