package cucumber.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shellwords {

    private static final Pattern SHELLWORDS_PATTERN;

    static {
        final String SHELLWORDS_FORMAT = "\\s*(?>([^\\s\\\\\\'\\\"]+)|'([^\\']*)'|\"((?:[^\\\"\\\\]|\\\\.)*)\"|(\\\\.?)|(\\S))(\\s|\\z)?";
        SHELLWORDS_PATTERN = Pattern.compile(SHELLWORDS_FORMAT);
    }

    private Shellwords() {
    }

    /**
     * <p>Parse command line input and extract all shellwords into a {@code List<String>}.</p>
     *
     * <p>Shellwords are whitespace seperated, apart from when a shellword starts with a Double Quotes {@code "}, or Single Quotes {@code '}.
     * Where a Quote starts a shellword, the shellword is only closed by another quote or the end of the line.</p>
     *
     * @param cmdline the command line arguments passed in
     * @return a {@code List<String>} of the individual words supplied.
     */
    public static List<String> parse(final String cmdline) {
        final List<String> matchList = new ArrayList<>();
        final Matcher shellwordsMatcher = SHELLWORDS_PATTERN.matcher(cmdline);
        while (shellwordsMatcher.find()) {
            // default token to group
            String token = shellwordsMatcher.group();
            // find highest group count that is not null and use that
            for (int i = shellwordsMatcher.groupCount() - 1; i > 0; --i) {
                final String groupToken = shellwordsMatcher.group(i);
                if (groupToken != null) {
                    token = groupToken;
                    break;
                }
            }
            matchList.add(token);
        }
        return matchList;
    }

}
