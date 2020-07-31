package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.plugin.Options;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.tagexpressions.Expression;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class RuntimeOptionsBuilder {

    private final List<Expression> parsedTagFilters = new ArrayList<>();
    private final List<Pattern> parsedNameFilters = new ArrayList<>();
    private final List<FeatureWithLines> parsedFeaturePaths = new ArrayList<>();
    private final List<URI> parsedGlue = new ArrayList<>();
    private final List<Options.Plugin> formatters = new ArrayList<>();
    private final List<Options.Plugin> summaryPrinters = new ArrayList<>();
    private List<FeatureWithLines> parsedRerunPaths = null;
    private Integer parsedThreads = null;
    private Boolean parsedDryRun = null;
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
    private String parsedPublishToken = null;
    private Boolean parsedPublish;
    private Boolean parsedPublishQuiet;
    private Boolean parsedEnablePublishPlugin;

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

    public RuntimeOptionsBuilder addPluginName(String pluginSpecification) {
        PluginOption pluginOption = PluginOption.parse(pluginSpecification);
        if (pluginOption.isSummaryPrinter()) {
            summaryPrinters.add(pluginOption);
        } else if (pluginOption.isFormatter()) {
            formatters.add(pluginOption);
        } else {
            throw new CucumberException("Unrecognized plugin: " + pluginSpecification);
        }
        return this;
    }

    public RuntimeOptionsBuilder addTagFilter(Expression tagExpression) {
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

        runtimeOptions.addFormatters(this.formatters);
        runtimeOptions.addSummaryPrinters(this.summaryPrinters);

        if (parsedObjectFactoryClass != null) {
            runtimeOptions.setObjectFactoryClass(parsedObjectFactoryClass);
        }

        if (addDefaultFormatterIfAbsent) {
            runtimeOptions.addDefaultFormatterIfAbsent();
        }

        if (addDefaultSummaryPrinterIfAbsent) {
            runtimeOptions.addDefaultSummaryPrinterIfAbsent();
        }

        if (addDefaultGlueIfAbsent) {
            runtimeOptions.addDefaultGlueIfAbsent();
        }

        if (addDefaultFeaturePathIfAbsent) {
            runtimeOptions.addDefaultFeaturePathIfAbsent();
        }

        if (parsedPublishToken != null) {
            runtimeOptions.setPublishToken(parsedPublishToken);
        }

        if (parsedPublish != null) {
            runtimeOptions.setPublish(parsedPublish);
        }

        if (parsedPublishQuiet != null) {
            runtimeOptions.setPublishQuiet(parsedPublishQuiet);
        }

        if (parsedEnablePublishPlugin != null) {
            runtimeOptions.setEnablePublishPlugin(parsedEnablePublishPlugin);
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

    public RuntimeOptionsBuilder setDryRun() {
        return setDryRun(true);
    }

    public RuntimeOptionsBuilder setDryRun(boolean dryRun) {
        this.parsedDryRun = dryRun;
        return this;
    }

    public RuntimeOptionsBuilder setMonochrome() {
        return setMonochrome(true);
    }

    public RuntimeOptionsBuilder setMonochrome(boolean monochrome) {
        this.parsedMonochrome = monochrome;
        return this;
    }

    public RuntimeOptionsBuilder setPickleOrder(PickleOrder pickleOrder) {
        this.parsedPickleOrder = pickleOrder;
        return this;
    }

    public RuntimeOptionsBuilder setSnippetType(SnippetType snippetType) {
        this.parsedSnippetType = snippetType;
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

    public RuntimeOptionsBuilder setObjectFactoryClass(Class<? extends ObjectFactory> objectFactoryClass) {
        this.parsedObjectFactoryClass = objectFactoryClass;
        return this;
    }

    public RuntimeOptionsBuilder setPublishToken(String token) {
        this.parsedPublishToken = token;
        return this;
    }

    public RuntimeOptionsBuilder setPublish(boolean publish) {
        this.parsedPublish = publish;
        return this;
    }

    public RuntimeOptionsBuilder setPublishQuiet(boolean publishQuiet) {
        this.parsedPublishQuiet = publishQuiet;
        return this;
    }

    public RuntimeOptionsBuilder enablePublishPlugin() {
        this.parsedEnablePublishPlugin = true;
        return this;
    }

}
