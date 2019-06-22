package io.cucumber.core.options;

import cucumber.api.SnippetType;
import cucumber.runtime.CucumberException;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.order.PickleOrder;
import io.cucumber.core.model.FeatureWithLines;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public final class RuntimeOptionsBuilder {
    private List<String> parsedTagFilters = new ArrayList<>();
    private List<Pattern> parsedNameFilters = new ArrayList<>();
    private Map<URI, Set<Integer>> parsedLineFilters = new HashMap<>();
    private List<URI> parsedFeaturePaths = new ArrayList<>();
    private List<URI> parsedGlue = new ArrayList<>();
    private ParsedPluginData parsedPluginData = new ParsedPluginData();
    private List<String> parsedJunitOptions = new ArrayList<>();
    private boolean parsedIsRerun = false;
    private Integer parsedThreads = null;
    private Boolean parsedDryRun = null;
    private Boolean parsedStrict = null;
    private Boolean parsedMonochrome = null;
    private SnippetType parsedSnippetType = null;
    private Boolean parsedWip = null;
    private PickleOrder parsedPickleOrder = null;
    private Integer parsedCount = null;

    public RuntimeOptionsBuilder addFeature(FeatureWithLines featureWithLines) {
        parsedFeaturePaths.add(featureWithLines.uri());
        addLineFilters(featureWithLines);
        return this;
    }

    public RuntimeOptionsBuilder addGlue(URI glue) {
        parsedGlue.add(glue);
        return this;
    }

    public RuntimeOptionsBuilder addJunitOption(String junitOption) {
        this.parsedJunitOptions.add(junitOption);
        return this;
    }

    private RuntimeOptionsBuilder addLineFilters(FeatureWithLines featureWithLines) {
        URI key = featureWithLines.uri();
        Set<Integer> lines = featureWithLines.lines();
        if (lines.isEmpty()) {
            return null;
        }
        if (this.parsedLineFilters.containsKey(key)) {
            this.parsedLineFilters.get(key).addAll(lines);
        } else {
            this.parsedLineFilters.put(key, new TreeSet<>(lines));
        }
        return this;
    }

    public RuntimeOptionsBuilder addNameFilter(Pattern pattern) {
        this.parsedNameFilters.add(pattern);
        return this;
    }

    public RuntimeOptionsBuilder addPluginName(String name, boolean isAddPlugin) {
        this.parsedPluginData.addPluginName(name, isAddPlugin);
        return this;
    }

    public RuntimeOptionsBuilder addTagFilter(String tagExpression) {
        this.parsedTagFilters.add(tagExpression);
        return this;
    }

    public RuntimeOptions build() {
        return build(RuntimeOptions.defaultOptions());
    }

    public RuntimeOptions build(RuntimeOptions runtimeOptions) {
        if (this.parsedThreads != null) {
            runtimeOptions.setThreads(this.parsedThreads);
        }

        if (this.parsedDryRun != null) {
            runtimeOptions.setDryRun(this.parsedDryRun);
        }

        if (this.parsedStrict != null) {
            runtimeOptions.setStrict(this.parsedStrict);
        }

        if (this.parsedMonochrome != null) {
            runtimeOptions.setMonochrome(this.parsedMonochrome);
        }

        if (this.parsedSnippetType != null) {
            runtimeOptions.setSnippetType(this.parsedSnippetType);
        }

        if (this.parsedWip != null) {
            runtimeOptions.setWip(this.parsedWip);
        }

        if (this.parsedPickleOrder != null) {
            runtimeOptions.setPickleOrder(this.parsedPickleOrder);
        }

        if (this.parsedCount != null) {
            runtimeOptions.setCount(this.parsedCount);
        }

        if (this.parsedIsRerun || !this.parsedFeaturePaths.isEmpty()) {
            runtimeOptions.setFeaturePaths(Collections.<URI>emptyList());
            runtimeOptions.setLineFilters(Collections.<URI, Set<Integer>>emptyMap());
        }
        if (!this.parsedTagFilters.isEmpty() || !this.parsedNameFilters.isEmpty() || !this.parsedLineFilters.isEmpty()) {
            runtimeOptions.setTagFilters(this.parsedTagFilters);
            runtimeOptions.setNameFilters(this.parsedNameFilters);
            runtimeOptions.setLineFilters(this.parsedLineFilters);
        }
        if (!this.parsedFeaturePaths.isEmpty()) {
            runtimeOptions.setFeaturePaths(this.parsedFeaturePaths);
        }

        if (!this.parsedGlue.isEmpty()) {
            runtimeOptions.setGlue(this.parsedGlue);
        }
        if (!this.parsedJunitOptions.isEmpty()) {
            runtimeOptions.setJunitOptions(this.parsedJunitOptions);
        }

        this.parsedPluginData.updatePluginFormatterNames(runtimeOptions.getPluginFormatterNames());
        this.parsedPluginData.updatePluginStepDefinitionReporterNames(runtimeOptions.getPluginStepDefinitionReporterNames());
        this.parsedPluginData.updatePluginSummaryPrinterNames(runtimeOptions.getPluginSummaryPrinterNames());

        return runtimeOptions;
    }

    public RuntimeOptionsBuilder setCount(int count) {
        this.parsedCount = count;
        return this;
    }

    public RuntimeOptionsBuilder setDryRun(boolean dryRun) {
        this.parsedDryRun = dryRun;
        return this;
    }

    public RuntimeOptionsBuilder setDryRun() {
        return setDryRun(true);
    }

    public void setIsRerun(boolean isRerun) {
        this.parsedIsRerun = isRerun;
    }

    public RuntimeOptionsBuilder setMonochrome(boolean monochrome) {
        this.parsedMonochrome = monochrome;
        return this;
    }

    public RuntimeOptionsBuilder setMonochrome() {
        return setMonochrome(true);
    }

    public RuntimeOptionsBuilder setPickleOrder(PickleOrder pickleOrder) {
        this.parsedPickleOrder = pickleOrder;
        return this;
    }

    public RuntimeOptionsBuilder setSnippetType(SnippetType snippetType) {
        this.parsedSnippetType = snippetType;
        return this;
    }

    public RuntimeOptionsBuilder setStrict() {
        return setStrict(true);
    }

    public RuntimeOptionsBuilder setStrict(boolean strict) {
        this.parsedStrict = strict;
        return this;
    }

    public RuntimeOptionsBuilder setThreads(int threads) {
        this.parsedThreads = threads;
        return this;
    }

    public RuntimeOptionsBuilder setWip(boolean wip) {
        this.parsedWip = wip;
        return this;
    }

    public RuntimeOptionsBuilder addDefaultSummaryPrinterIfNotPresent() {
        parsedPluginData.addDefaultSummaryPrinterIfNotPresent();
        return this;
    }

    public RuntimeOptionsBuilder addDefaultFormatterIfNotPresent() {
        parsedPluginData.addDefaultFormatterIfNotPresent();
        return this;
    }

    private static class ParsedPluginData {
        ParsedOptionNames formatterNames = new ParsedOptionNames();
        ParsedOptionNames stepDefinitionReporterNames = new ParsedOptionNames();
        ParsedOptionNames summaryPrinterNames = new ParsedOptionNames();

        void addPluginName(String name, boolean isAddPlugin) {
            if (PluginFactory.isStepDefinitionReporterName(name)) {
                stepDefinitionReporterNames.addName(name, isAddPlugin);
            } else if (PluginFactory.isSummaryPrinterName(name)) {
                summaryPrinterNames.addName(name, isAddPlugin);
            } else if (PluginFactory.isFormatterName(name)) {
                formatterNames.addName(name, isAddPlugin);
            } else {
                throw new CucumberException("Unrecognized plugin: " + name);
            }
        }

        void updatePluginFormatterNames(List<String> pluginFormatterNames) {
            formatterNames.updateNameList(pluginFormatterNames);
        }

        void updatePluginStepDefinitionReporterNames(List<String> pluginStepDefinitionReporterNames) {
            stepDefinitionReporterNames.updateNameList(pluginStepDefinitionReporterNames);
        }

        void updatePluginSummaryPrinterNames(List<String> pluginSummaryPrinterNames) {
            summaryPrinterNames.updateNameList(pluginSummaryPrinterNames);
        }


        void addDefaultSummaryPrinterIfNotPresent() {
            if (summaryPrinterNames.names.isEmpty()) {
                summaryPrinterNames.addName("default_summary", false);
            }
        }

        void addDefaultFormatterIfNotPresent() {
            if (formatterNames.names.isEmpty()) {
                formatterNames.addName("progress", false);
            }
        }
    }

    private static class ParsedOptionNames {
        private List<String> names = new ArrayList<>();
        private boolean clobber = false;

        void addName(String name, boolean isAddOption) {
            names.add(name);
            if (!isAddOption) {
                clobber = true;
            }
        }

        void updateNameList(List<String> nameList) {
            if (!names.isEmpty()) {
                if (clobber) {
                    nameList.clear();
                }
                nameList.addAll(names);
            }
        }
    }
}
