package cucumber.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shellwords {

    private static final Pattern SHELLWORDS_PATTERN = Pattern.compile("[^\\s'\"]+|[']([^']*)[']|[\"]([^\"]*)[\"]");

    private Shellwords() {
    }

    public static List<String> parse(String cmdline) {
        List<String> matchList = new ArrayList<>();
        Matcher shellwordsMatcher = SHELLWORDS_PATTERN.matcher(cmdline);
        while (shellwordsMatcher.find()) {
            if (shellwordsMatcher.group(1) != null) {
                matchList.add(shellwordsMatcher.group(1));
            } else {
                String token = shellwordsMatcher.group();
                boolean singleQuoted = token.startsWith("'")
                    && token.endsWith("'");
                boolean doubleQuoted = token.startsWith("\"")
                    && token.endsWith("\"");
                boolean quoted = singleQuoted || doubleQuoted;
                if (quoted
                        && token.length() > 2) {
                    token = token.substring(1, token.length() - 1);
                }
                matchList.add(token);
            }
        }
        return matchList;
    }

}
