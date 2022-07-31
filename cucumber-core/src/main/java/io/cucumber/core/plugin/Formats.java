package io.cucumber.core.plugin;

import java.util.HashMap;
import java.util.Map;

import static io.cucumber.core.plugin.Format.color;

interface Formats {

    Format get(String key);

    String up(int n);

    static Formats monochrome() {
        return new Monochrome();
    }

    static Formats ansi() {
        return new Ansi();
    }

    final class Monochrome implements Formats {

        private Monochrome() {

        }

        public Format get(String key) {
            return text -> text;
        }

        public String up(int n) {
            return "";
        }

    }

    final class Ansi implements Formats {

        private Ansi() {

        }

        private static final Map<String, Format> formats = new HashMap<String, Format>() {
            {
                // Never used, but avoids NPE in formatters.
                put("undefined", color(AnsiEscapes.YELLOW));
                put("undefined_arg", color(AnsiEscapes.YELLOW, AnsiEscapes.INTENSITY_BOLD));
                put("unused", color(AnsiEscapes.YELLOW));
                put("unused_arg", color(AnsiEscapes.YELLOW, AnsiEscapes.INTENSITY_BOLD));
                put("pending", color(AnsiEscapes.YELLOW));
                put("pending_arg", color(AnsiEscapes.YELLOW, AnsiEscapes.INTENSITY_BOLD));
                put("executing", color(AnsiEscapes.GREY));
                put("executing_arg", color(AnsiEscapes.GREY, AnsiEscapes.INTENSITY_BOLD));
                put("failed", color(AnsiEscapes.RED));
                put("failed_arg", color(AnsiEscapes.RED, AnsiEscapes.INTENSITY_BOLD));
                put("ambiguous", color(AnsiEscapes.RED));
                put("ambiguous_arg", color(AnsiEscapes.RED, AnsiEscapes.INTENSITY_BOLD));
                put("passed", color(AnsiEscapes.GREEN));
                put("passed_arg", color(AnsiEscapes.GREEN, AnsiEscapes.INTENSITY_BOLD));
                put("outline", color(AnsiEscapes.CYAN));
                put("outline_arg", color(AnsiEscapes.CYAN, AnsiEscapes.INTENSITY_BOLD));
                put("skipped", color(AnsiEscapes.CYAN));
                put("skipped_arg", color(AnsiEscapes.CYAN, AnsiEscapes.INTENSITY_BOLD));
                put("comment", color(AnsiEscapes.GREY));
                put("tag", color(AnsiEscapes.CYAN));
                put("output", color(AnsiEscapes.BLUE));
            }
        };

        public Format get(String key) {
            Format format = formats.get(key);
            if (format == null)
                throw new NullPointerException("No format for key " + key);
            return format;
        }

        public String up(int n) {
            return AnsiEscapes.up(n).toString();
        }

    }

}
