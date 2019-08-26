package cucumber.runtime.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathWithLines {
    private static final Pattern FILE_COLON_LINE_PATTERN = Pattern.compile("^([\\w\\W]*?):([\\d:]+)$");

    public final String path;
    public final List<Long> lines = new ArrayList<Long>();

    public static boolean hasLineFilters(String pathName) {
        return FILE_COLON_LINE_PATTERN.matcher(pathName).matches();
    }

    public static String stripLineFilters(String pathName) {
        Matcher matcher = FILE_COLON_LINE_PATTERN.matcher(pathName);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return pathName;
        }
    }

    public PathWithLines(String pathName) {
        Matcher matcher = FILE_COLON_LINE_PATTERN.matcher(pathName);
        if (matcher.matches()) {
            path = matcher.group(1);
            lines.addAll(toLongs(matcher.group(2).split(":")));
        } else {
            path = pathName;
        }
    }

    private static List<Long> toLongs(String[] strings) {
        List<Long> result = new ArrayList<Long>();
        for (String string : strings) {
            result.add(Long.parseLong(string));
        }
        return result;
    }

    public String toString() {
        return path + ":" + lines;
    }
}
