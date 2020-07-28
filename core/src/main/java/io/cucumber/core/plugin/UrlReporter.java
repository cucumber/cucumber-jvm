package io.cucumber.core.plugin;

import io.cucumber.plugin.ColorAware;

import java.io.PrintStream;
import java.net.URL;

final class UrlReporter implements ColorAware {
    private final PrintStream out;
    private Formats formats = new AnsiFormats();

    public UrlReporter(PrintStream out) {
        this.out = out;
    }

    public void report(URL url) {
        String path = url.getPath();
        int pathLength = path.length();
        Format format = formats.get("skipped");
        StringBuilder out = new StringBuilder();
        out.append(format.text("┌─────────────────────────────" + times('─', pathLength) + "┐")).append("\n");
        out.append(format.text("│")).append(" View your Cucumber Report at:").append(times(' ', pathLength - 1))
                .append(format.text("│")).append("\n");
        out.append(format.text("│")).append(" https://reports.cucumber.io").append(path).append(" ")
                .append(format.text("│")).append("\n");
        out.append(format.text("└─────────────────────────────" + times('─', pathLength) + "┘")).append("\n");
        this.out.print(out.toString());
    }

    private String times(char c, int count) {
        return new String(new char[count]).replace('\0', c);
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        if (monochrome) {
            formats = new MonochromeFormats();
        } else {
            formats = new AnsiFormats();
        }
    }

}
