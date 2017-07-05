package cucumber.runtime.formatter;

import cucumber.api.formatter.AnsiEscapes;

import java.util.HashMap;
import java.util.Map;

public class AnsiFormats implements Formats {
    private static final Map<String, Format> formats = new HashMap<String, Format>() {{
        put("undefined", new ColorFormat(AnsiEscapes.YELLOW));
        put("undefined_arg", new ColorFormat(AnsiEscapes.YELLOW, AnsiEscapes.INTENSITY_BOLD)); // Never used, but avoids NPE in formatters.
        put("pending", new ColorFormat(AnsiEscapes.YELLOW));
        put("pending_arg", new ColorFormat(AnsiEscapes.YELLOW, AnsiEscapes.INTENSITY_BOLD));
        put("executing", new ColorFormat(AnsiEscapes.GREY));
        put("executing_arg", new ColorFormat(AnsiEscapes.GREY, AnsiEscapes.INTENSITY_BOLD));
        put("failed", new ColorFormat(AnsiEscapes.RED));
        put("failed_arg", new ColorFormat(AnsiEscapes.RED, AnsiEscapes.INTENSITY_BOLD));
        put("ambiguous", new ColorFormat(AnsiEscapes.RED));
        put("ambiguous_arg", new ColorFormat(AnsiEscapes.RED, AnsiEscapes.INTENSITY_BOLD));
        put("passed", new ColorFormat(AnsiEscapes.GREEN));
        put("passed_arg", new ColorFormat(AnsiEscapes.GREEN, AnsiEscapes.INTENSITY_BOLD));
        put("outline", new ColorFormat(AnsiEscapes.CYAN));
        put("outline_arg", new ColorFormat(AnsiEscapes.CYAN, AnsiEscapes.INTENSITY_BOLD));
        put("skipped", new ColorFormat(AnsiEscapes.CYAN));
        put("skipped_arg", new ColorFormat(AnsiEscapes.CYAN, AnsiEscapes.INTENSITY_BOLD));
        put("comment", new ColorFormat(AnsiEscapes.GREY));
        put("tag", new ColorFormat(AnsiEscapes.CYAN));
        put("output", new ColorFormat(AnsiEscapes.BLUE));
    }};

    public static class ColorFormat implements Format {
        private final AnsiEscapes[] escapes;

        public ColorFormat(AnsiEscapes... escapes) {
            this.escapes = escapes;
        }

        public String text(String text) {
            StringBuilder sb = new StringBuilder();
            for (AnsiEscapes escape : escapes) {
                escape.appendTo(sb);
            }
            sb.append(text);
            AnsiEscapes.RESET.appendTo(sb);
            return sb.toString();
        }
    }

    public Format get(String key) {
        Format format = formats.get(key);
        if (format == null) throw new NullPointerException("No format for key " + key);
        return format;
    }

    public String up(int n) {
        return AnsiEscapes.up(n).toString();
    }
}
