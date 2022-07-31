package io.cucumber.core.plugin;

import io.cucumber.plugin.ColorAware;

import java.io.PrintStream;

final class UrlReporter implements ColorAware {
    private final PrintStream out;
    private boolean monochrome;

    public UrlReporter(PrintStream out) {
        this.out = out;
    }

    public void report(String message) {
        if (monochrome) {
            message = message.replaceAll("\u001B\\[[;\\d]*m", "");
        }
        out.print(message);
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }

}
