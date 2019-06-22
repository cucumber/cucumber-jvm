package io.cucumber.core.options;

import cucumber.api.SnippetType;
import cucumber.runtime.order.PickleOrder;
import cucumber.runtime.order.StandardPickleOrders;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public final class RuntimeOptions implements FeatureOptions, FilterOptions, PluginOptions, RunnerOptions {

    private final List<URI> glue = new ArrayList<>();
    private final List<String> tagFilters = new ArrayList<>();
    private final List<Pattern> nameFilters = new ArrayList<>();
    private final Map<URI, Set<Integer>> lineFilters = new HashMap<>();
    private final SortedSet<URI> featurePaths = new TreeSet<>();

    private final List<String> junitOptions = new ArrayList<>();
    private boolean dryRun;
    private boolean strict = false;
    private boolean monochrome = false;
    private boolean wip = false;
    private SnippetType snippetType = SnippetType.UNDERSCORE;
    private int threads = 1;
    private PickleOrder pickleOrder = StandardPickleOrders.lexicalUriOrder();
    private int count = 0;

    private final List<String> pluginFormatterNames = new ArrayList<>();
    private final List<String> pluginStepDefinitionReporterNames = new ArrayList<>();
    private final List<String> pluginSummaryPrinterNames = new ArrayList<>();

    private RuntimeOptions() {

    }

    public static RuntimeOptions defaultOptions() {
        return new RuntimeOptions();
    }

    public int getCount() {
        return count;
    }

    List<String> getPluginFormatterNames() {
        return pluginFormatterNames;
    }

    List<String> getPluginStepDefinitionReporterNames() {
        return pluginStepDefinitionReporterNames;
    }

    List<String> getPluginSummaryPrinterNames() {
        return pluginSummaryPrinterNames;
    }

    public boolean isMultiThreaded() {
        return getThreads() > 1;
    }

    @Override
    public List<String> getPluginNames() {
        List<String> pluginNames = new ArrayList<>();
        pluginNames.addAll(getPluginFormatterNames());
        pluginNames.addAll(getPluginStepDefinitionReporterNames());
        pluginNames.addAll(getPluginSummaryPrinterNames());
        return pluginNames;
    }

    @Override
    public List<URI> getGlue() {
        return unmodifiableList(glue);
    }

    @Override
    public boolean isStrict() {
        return strict;
    }

    @Override
    public boolean isDryRun() {
        return dryRun;
    }

    public boolean isWip() {
        return wip;
    }

    @Override
    public List<URI> getFeaturePaths() {
        return unmodifiableList(new ArrayList<>(featurePaths));
    }

    @Override
    public List<Pattern> getNameFilters() {
        return unmodifiableList(nameFilters);
    }

    @Override
    public List<String> getTagFilters() {
        return unmodifiableList(tagFilters);
    }

    void setCount(int count) {
        this.count = count;
    }

    void setFeaturePaths(List<URI> featurePaths) {
        this.featurePaths.clear();
        this.featurePaths.addAll(featurePaths);
    }

    void setGlue(List<URI> parsedGlue) {
        glue.clear();
        glue.addAll(parsedGlue);
    }

    void setJunitOptions(List<String> junitOptions) {
        this.junitOptions.clear();
        this.junitOptions.addAll(junitOptions);
    }

    void setLineFilters(Map<URI, Set<Integer>> lineFilters) {
        this.lineFilters.clear();
        for (URI path : lineFilters.keySet()) {
            this.lineFilters.put(path, lineFilters.get(path));
        }
    }

    void setNameFilters(List<Pattern> nameFilters) {
        this.nameFilters.clear();
        this.nameFilters.addAll(nameFilters);
    }

    void setPickleOrder(PickleOrder pickleOrder) {
        this.pickleOrder = pickleOrder;
    }

    void setTagFilters(List<String> tagFilters) {
        this.tagFilters.clear();
        this.tagFilters.addAll(tagFilters);
    }

    @Override
    public Map<URI, Set<Integer>> getLineFilters() {
        return unmodifiableMap(new HashMap<>(lineFilters));
    }

    @Override
    public int getLimitCount() {
        return getCount();
    }

    @Override
    public boolean isMonochrome() {
        return monochrome;
    }

    @Override
    public SnippetType getSnippetType() {
        return snippetType;
    }

    public List<String> getJunitOptions() {
        return unmodifiableList(junitOptions);
    }

    public int getThreads() {
        return threads;
    }

    public PickleOrder getPickleOrder() {
        return pickleOrder;
    }

    void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }

    void setSnippetType(SnippetType snippetType) {
        this.snippetType = snippetType;
    }

    void setStrict(boolean strict) {
        this.strict = strict;
    }

    void setThreads(int threads) {
        this.threads = threads;
    }

    void setWip(boolean wip) {
        this.wip = wip;
    }
}
