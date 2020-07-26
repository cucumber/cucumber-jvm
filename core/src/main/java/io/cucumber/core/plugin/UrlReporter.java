package io.cucumber.core.plugin;

import io.cucumber.core.exception.CucumberException;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;

class UrlReporter {
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";
    private final Writer out;

    public UrlReporter(Writer out) {
        this.out = out;
    }

    public void report(URL url) {
        String path = url.getPath();
        int pathLength = path.length();
        try {
            out.append(CYAN).append("┌─────────────────────────────").append(times('─', pathLength)).append("┐")
                    .append(RESET).append("\n");
            out.append(CYAN).append("│").append(RESET).append(" View your Cucumber Report at:")
                    .append(times(' ', pathLength - 1)).append(CYAN).append("│").append(RESET).append("\n");
            out.append(CYAN).append("│").append(RESET).append(" https://reports.cucumber.io").append(path).append(" ")
                    .append(CYAN).append("│").append(RESET).append("\n");
            out.append(CYAN).append("└─────────────────────────────").append(times('─', pathLength)).append("┘")
                    .append(RESET).append("\n");
            out.flush();
        } catch (IOException e) {
            throw new CucumberException("Couldn't report report URL", e);
        }
    }

    private String times(char c, int count) {
        return new String(new char[count]).replace('\0', c);
    }
}
