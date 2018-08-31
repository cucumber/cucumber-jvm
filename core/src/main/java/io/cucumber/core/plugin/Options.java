package io.cucumber.core.plugin;

import java.util.List;

public interface Options {
    List<String> getPluginFormatterNames();

    List<String> getPluginStepDefinitionReporterNames();

    List<String> getPluginSummaryPrinterNames();

    boolean isMonochrome();

    boolean isStrict();
}
