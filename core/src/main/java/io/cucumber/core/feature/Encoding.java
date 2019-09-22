package io.cucumber.core.feature;

import io.cucumber.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;

/**
 * Utilities for reading the encoding of a file.
 */
final class Encoding {
    private static final Pattern COMMENT_OR_EMPTY_LINE_PATTERN = Pattern.compile("^\\s*#|^\\s*$");
    private static final Pattern ENCODING_PATTERN = Pattern.compile("^\\s*#\\s*encoding\\s*:\\s*([0-9a-zA-Z\\-]+)", Pattern.CASE_INSENSITIVE);
    private static final String DEFAULT_ENCODING = UTF_8.name();
    private static final String UTF_8_BOM = "\uFEFF";

    static String readFile(Resource resource) throws RuntimeException, IOException {
        String source = read(resource, DEFAULT_ENCODING);
        // Remove UTF8 BOM encoded in first bytes
        if (source.startsWith(UTF_8_BOM)) {
            source = source.replaceFirst(UTF_8_BOM, "");
        }
        String enc = encoding(source);
        if (!enc.equals(DEFAULT_ENCODING)) {
            source = read(resource, enc);
        }
        return source;
    }

    private static String read(Resource resource, String encoding) throws IOException {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), encoding))){
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
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
