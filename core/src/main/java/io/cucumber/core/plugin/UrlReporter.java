package io.cucumber.core.plugin;

import io.cucumber.plugin.ColorAware;

import java.io.PrintStream;
import java.net.URL;

import static java.util.Arrays.asList;

final class UrlReporter implements ColorAware {
    private final PrintStream out;
    private boolean monochrome;

    public UrlReporter(PrintStream out) {
        this.out = out;
    }

    public void report(URL url) {
        String reportUrl = String.format("https://reports.cucumber.io%s", url.getPath());

        Banner banner = new Banner(out, monochrome);
        banner.print(asList(
            new Banner.Line("View your Cucumber Report at:"),
            new Banner.Line(reportUrl, AnsiEscapes.CYAN, AnsiEscapes.INTENSITY_BOLD),
            new Banner.Line(""),
            new Banner.Line("This report will self-destruct in 24h unless it is claimed or deleted.")),
            AnsiEscapes.GREEN, AnsiEscapes.INTENSITY_BOLD);
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }

}
