package cucumber.util;

import cucumber.runtime.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.ROOT;

/**
 * Utilities for reading the encoding of a file.
 */
public class Encoding {
    private static final Pattern COMMENT_OR_EMPTY_LINE_PATTERN = Pattern.compile("^\\s*#|^\\s*$");
    private static final Pattern ENCODING_PATTERN = Pattern.compile("^\\s*#\\s*encoding\\s*:\\s*([0-9a-zA-Z\\-]+)", Pattern.CASE_INSENSITIVE);
    public static final String DEFAULT_ENCODING = "UTF-8";
    private static final String UTF_8_BOM = "\uFEFF";

    public static String readFile(Resource resource) throws RuntimeException, IOException {
        String source = FixJava.readReader(new InputStreamReader(resource.getInputStream(), DEFAULT_ENCODING));
     // Remove UTF8 BOM encoded in first bytes
     	if (source.startsWith(UTF_8_BOM)) {
     		source = source.replaceFirst(UTF_8_BOM, "");
     	}
        String enc = encoding(source);
        if(!enc.equals(DEFAULT_ENCODING)) {
            source = FixJava.readReader(new InputStreamReader(resource.getInputStream(), enc));
        }
        return source;
    }

    private static String encoding(String source) {
        String encoding = DEFAULT_ENCODING;
        for (String line : source.split("\\n")) {
            if (!COMMENT_OR_EMPTY_LINE_PATTERN.matcher(line).find()) {
                break;
            }
            Matcher matcher = ENCODING_PATTERN.matcher(line);
            if (matcher.find()) {
                encoding = matcher.group(1);
                break;
            }
        }
        return encoding.toUpperCase(ROOT);
    }
}
