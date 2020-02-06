package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.plugin.Options;
import io.cucumber.core.snippets.SnippetType;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class RuntimeOptionsBuilder {
    private final List<String> parsedTagFilters = new ArrayList<>();
    private final List<Pattern> parsedNameFilters = new ArrayList<>();
    private final List<FeatureWithLines> parsedFeaturePaths = new ArrayList<>();
    private final List<URI> parsedGlue = new ArrayList<>();
    private final ParsedPluginData parsedPluginData = new ParsedPluginData();
    private List<FeatureWithLines> parsedRerunPaths = null;
    private Integer parsedThreads = null;
    private Boolean parsedDryRun = null;
    private Boolean parsedStrict = null;
    private Boolean parsedMonochrome = null;
    private SnippetType parsedSnippetType = null;
    private Boolean parsedWip = null;
    private PickleOrder parsedPickleOrder = null;
    private Integer parsedCount = null;
    private Class<? extends ObjectFactory> parsedObjectFactoryClass = null;
    private boolean addDefaultSummaryPrinterIfAbsent;
    private boolean addDefaultFormatterIfAbsent;
    private boolean addDefaultGlueIfAbsent;
    private boolean addDefaultFeaturePathIfAbsent;

    public RuntimeOptionsBuilder addRerun(Collection<FeatureWithLines> featureWithLines) {
        if (parsedRerunPaths == null) {
            parsedRerunPaths = new ArrayList<>();
        }
        parsedRerunPaths.addAll(featureWithLines);
        return this;
    }

    public RuntimeOptionsBuilder addFeature(FeatureWithLines featureWithLines) {
        parsedFeaturePaths.add(featureWithLines);
        return this;
    }

    public RuntimeOptionsBuilder addGlue(URI glue) {
        parsedGlue.add(glue);
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

        if (!this.parsedTagFilters.isEmpty() || !this.parsedNameFilters.isEmpty() || hasFeaturesWithLineFilters()) {
            runtimeOptions.setTagExpressions(this.parsedTagFilters);
            runtimeOptions.setNameFilters(this.parsedNameFilters);
        }
        if (!this.parsedFeaturePaths.isEmpty() || this.parsedRerunPaths != null) {
            List<FeatureWithLines> features = new ArrayList<>(this.parsedFeaturePaths);
            if (parsedRerunPaths != null) {
                features.addAll(this.parsedRerunPaths);
            }
            runtimeOptions.setFeaturePaths(features);
        }

        if (!this.parsedGlue.isEmpty()) {
            runtimeOptions.setGlue(this.parsedGlue);
        }

        this.parsedPluginData.updateFormatters(runtimeOptions.getFormatters());
        this.parsedPluginData.updateSummaryPrinters(runtimeOptions.getSummaryPrinter());

        if (parsedObjectFactoryClass != null) {
            runtimeOptions.setObjectFactoryClass(parsedObjectFactoryClass);
        }

        if(addDefaultFormatterIfAbsent) {
            runtimeOptions.addDefaultFormatterIfAbsent();
        }

        if(addDefaultSummaryPrinterIfAbsent) {
            runtimeOptions.addDefaultSummaryPrinterIfAbsent();
        }

        if (addDefaultGlueIfAbsent) {
            runtimeOptions.addDefaultGlueIfAbsent();
        }

        if (addDefaultFeaturePathIfAbsent) {
            runtimeOptions.addDefaultFeaturePathIfAbsent();
        }

        return runtimeOptions;
    }

    private boolean hasFeaturesWithLineFilters() {
        return parsedRerunPaths != null || !parsedFeaturePaths.stream()
            .map(FeatureWithLines::lines)
            .allMatch(Set::isEmpty);
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

    public RuntimeOptionsBuilder addDefaultSummaryPrinterIfAbsent() {
        this.addDefaultSummaryPrinterIfAbsent = true;
        return this;
    }

    public RuntimeOptionsBuilder addDefaultFormatterIfAbsent() {
        this.addDefaultFormatterIfAbsent = true;
        return this;
    }

    public RuntimeOptionsBuilder addDefaultGlueIfAbsent() {
        this.addDefaultGlueIfAbsent = true;
        return this;
    }

    public RuntimeOptionsBuilder addDefaultFeaturePathIfAbsent() {
        this.addDefaultFeaturePathIfAbsent = true;
        return this;
    }


    public void setObjectFactoryClass(Class<? extends ObjectFactory> objectFactoryClass) {
        this.parsedObjectFactoryClass = objectFactoryClass;
    }

    static final class ParsedPluginData {
        private final ParsedPlugins formatters = new ParsedPlugins();
        private final ParsedPlugins summaryPrinters = new ParsedPlugins();

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

        void updateFormatters(List<Options.Plugin> formatter) {
            this.formatters.updateNameList(formatter);
        }

        void updateSummaryPrinters(List<Options.Plugin> pluginSummaryPrinterNames) {
            summaryPrinters.updateNameList(pluginSummaryPrinterNames);
        }

        private static class ParsedPlugins {
            private final List<Options.Plugin> names = new ArrayList<>();
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
