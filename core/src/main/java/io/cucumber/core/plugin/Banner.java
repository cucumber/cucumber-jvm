package io.cucumber.core.plugin;

import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;

import static io.cucumber.core.plugin.Format.color;
import static io.cucumber.core.plugin.Format.monochrome;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparingInt;

final class Banner {
    private final boolean monochrome;

    static final class Line {
        private final List<Span> spans;

        Line(Span... spans) {
            this.spans = asList(spans);
        }

        Line(String text, AnsiEscapes... escapes) {
            this(new Span(text, escapes));
        }

        int length() {
            return spans.stream().map(span -> span.text.length()).mapToInt(Integer::intValue).sum();
        }
    }

    static final class Span {
        private final String text;
        private final AnsiEscapes[] escapes;

        Span(String text) {
            this.text = text;
            this.escapes = new AnsiEscapes[0];
        }

        Span(String text, AnsiEscapes... escapes) {
            this.text = text;
            this.escapes = escapes;
        }
    }

    private final PrintStream out;

    Banner(PrintStream out, boolean monochrome) {
        this.out = out;
        this.monochrome = monochrome;
    }

    void print(List<Line> lines, AnsiEscapes... border) {
        int maxLength = lines.stream().map(Line::length).max(comparingInt(a -> a))
                .orElseThrow(NoSuchElementException::new);

        StringBuilder out = new StringBuilder();

        Format borderFormat = monochrome ? monochrome() : color(border);

        out.append(borderFormat.text("┌" + times('─', maxLength + 2) + "┐")).append("\n");
        for (Line line : lines) {
            int rightPad = maxLength - line.length();
            out.append(borderFormat.text("│"))
                    .append(' ');

            for (Span span : line.spans) {
                Format format = monochrome ? monochrome() : color(span.escapes);
                out.append(format.text(span.text));
            }

            out.append(times(' ', rightPad))
                    .append(' ')
                    .append(borderFormat.text("│"))
                    .append("\n");

        }
        out.append(borderFormat.text("└" + times('─', maxLength + 2) + "┘")).append("\n");
        this.out.print(out);
    }

    private String times(char c, int count) {
        return new String(new char[count]).replace('\0', c);
    }
}
