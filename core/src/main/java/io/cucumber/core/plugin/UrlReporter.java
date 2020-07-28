package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.plugin.ColorAware;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;

final class UrlReporter implements ColorAware {
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";
    private final Writer out;
    private Formats formats = new AnsiFormats();

    public UrlReporter(Writer out) {
        this.out = out;
    }

    public void report(URL url) {
        String path = url.getPath();
        int pathLength = path.length();
        try {
            Format format = formats.get("skipped");
            out.append(format.text("┌─────────────────────────────"+ times('─', pathLength) + "┐")).append("\n");
            out.append(format.text("│")).append(" View your Cucumber Report at:").append(times(' ', pathLength - 1)).append(format.text("│")).append("\n");
            out.append(format.text("│")).append(" https://reports.cucumber.io").append(path).append(" ").append(format.text("│")).append("\n");
            out.append(format.text("└─────────────────────────────"+times('─', pathLength)+"┘")).append("\n");
            out.flush();
        } catch (IOException e) {
            throw new CucumberException("Couldn't report report URL", e);
        }
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
