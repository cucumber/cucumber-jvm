package io.cucumber.core.options;

import io.cucumber.core.snippets.SnippetType;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.plugin.Options;

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
            runtimeOptions.setTagExpressions(this.parsedTagFilters);
            runtimeOptions.setNameFilters(this.parsedNameFilters);
            runtimeOptions.setLineFilters(this.parsedLineFilters);
        }
        if (!this.parsedFeaturePaths.isEmpty()) {
            runtimeOptions.setFeaturePaths(this.parsedFeaturePaths);
        }

        if (!this.parsedGlue.isEmpty()) {
            runtimeOptions.setGlue(this.parsedGlue);
        }

        this.parsedPluginData.updateFormatters(runtimeOptions.getFormatters());
        this.parsedPluginData.updateSummaryPrinters(runtimeOptions.getSummaryPrinter());

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

    static final class ParsedPluginData {
        private ParsedPlugins formatters = new ParsedPlugins();
        private ParsedPlugins summaryPrinters = new ParsedPlugins();

        void addPluginName(String name, boolean isAddPlugin) {
            PluginOption pluginOption = PluginOption.parse(name);
            if (pluginOption.isSummaryPrinter()) {
                summaryPrinters.addName(pluginOption, isAddPlugin);
            } else if (pluginOption.isFormatter()) {
                formatters.addName(pluginOption, isAddPlugin);
            } else {
                throw new CucumberException("Unrecognized plugin: " + name);
            }
        }

        void addDefaultSummaryPrinterIfNotPresent() {
            if (summaryPrinters.names.isEmpty()) {
                addPluginName("summary", false);
            }
        }

        void addDefaultFormatterIfNotPresent() {
            if (formatters.names.isEmpty()) {
                addPluginName("progress", false);
            }
        }

        void updateFormatters(List<Options.Plugin> formatter) {
            this.formatters.updateNameList(formatter);
        }

        void updateSummaryPrinters(List<Options.Plugin> pluginSummaryPrinterNames) {
            summaryPrinters.updateNameList(pluginSummaryPrinterNames);
        }

        private static class ParsedPlugins {
            private List<Options.Plugin> names = new ArrayList<>();
            private boolean clobber = false;

            void addName(Options.Plugin name, boolean isAddOption) {
                names.add(name);
                if (!isAddOption) {
                    clobber = true;
                }
            }

            void updateNameList(List<Options.Plugin> nameList) {
                if (!names.isEmpty()) {
                    if (clobber) {
                        nameList.clear();
                    }
                    nameList.addAll(names);
                }
            }
        }
    }
}
