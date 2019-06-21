package cucumber.runtime;

import cucumber.api.SnippetType;
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
    private RuntimeOptionsParser.ParsedPluginData parsedPluginData = new RuntimeOptionsParser.ParsedPluginData();
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

    public void addFeature(FeatureWithLines featureWithLines) {
        parsedFeaturePaths.add(featureWithLines.uri());
        addLineFilters(featureWithLines);
    }

    public void addGlue(URI glue) {
        parsedGlue.add(glue);
    }

    public void addJunitOption(String junitOption) {
        this.parsedJunitOptions.add(junitOption);
    }

    private void addLineFilters(FeatureWithLines featureWithLines) {
        URI key = featureWithLines.uri();
        Set<Integer> lines = featureWithLines.lines();
        if (lines.isEmpty()) {
            return;
        }
        if (this.parsedLineFilters.containsKey(key)) {
            this.parsedLineFilters.get(key).addAll(lines);
        } else {
            this.parsedLineFilters.put(key, new TreeSet<>(lines));
        }
    }

    public void addNameFilter(Pattern pattern) {
        this.parsedNameFilters.add(pattern);
    }

    public void addPluginName(String name, boolean isAddPlugin) {
        this.parsedPluginData.addPluginName(name, isAddPlugin);
    }

    public void addTagFilter(String tagExpression) {
        this.parsedTagFilters.add(tagExpression);
    }

    public RuntimeOptions build(){
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

    public void setCount(int count) {
        this.parsedCount = count;
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

    public void setPickleOrder(PickleOrder pickleOrder) {
        this.parsedPickleOrder = pickleOrder;
    }

    public void setSnippetType(SnippetType snippetType) {
        this.parsedSnippetType = snippetType;
    }

    public RuntimeOptionsBuilder setStrict() {
        return setStrict(true);
    }
    public RuntimeOptionsBuilder setStrict(boolean strict) {
        this.parsedStrict = strict;
        return this;
    }

    public void setThreads(int threads) {
        this.parsedThreads = threads;
    }

    public void setWip(boolean wip) {
        this.parsedWip = wip;
    }
}
