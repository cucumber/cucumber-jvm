package io.cucumber.core.options;

import io.cucumber.core.snippets.SnippetType;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.StandardPickleOrders;

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

public final class RuntimeOptions implements
    io.cucumber.core.feature.Options,
    io.cucumber.core.runner.Options,
    io.cucumber.core.plugin.Options,
    io.cucumber.core.filter.Options {

    private final List<URI> glue = new ArrayList<>();
    private final List<String> tagExpressions = new ArrayList<>();
    private final List<Pattern> nameFilters = new ArrayList<>();
    private final Map<URI, Set<Integer>> lineFilters = new HashMap<>();
    private final SortedSet<URI> featurePaths = new TreeSet<>();

    private boolean dryRun;
    private boolean strict = false;
    private boolean monochrome = false;
    private boolean wip = false;
    private SnippetType snippetType = SnippetType.UNDERSCORE;
    private int threads = 1;
    private PickleOrder pickleOrder = StandardPickleOrders.lexicalUriOrder();
    private int count = 0;

    private final List<Plugin> formatters = new ArrayList<>();
    private final List<Plugin> summaryPrinters = new ArrayList<>();

    private RuntimeOptions() {

    }

    public static RuntimeOptions defaultOptions() {
        return new RuntimeOptions();
    }

    public int getCount() {
        return count;
    }

    List<Plugin> getFormatters() {
        return formatters;
    }

    List<Plugin> getSummaryPrinter() {
        return summaryPrinters;
    }

    public boolean isMultiThreaded() {
        return getThreads() > 1;
    }

    @Override
    public List<Plugin> plugins() {
        List<Plugin> plugins = new ArrayList<>();
        plugins.addAll(formatters);
        plugins.addAll(summaryPrinters);
        return plugins;
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
    public List<String> getTagExpressions() {
        return unmodifiableList(tagExpressions);
    }

    @Override
    public List<Pattern> getNameFilters() {
        return unmodifiableList(nameFilters);
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

    void setTagExpressions(List<String> tagExpressions) {
        this.tagExpressions.clear();
        this.tagExpressions.addAll(tagExpressions);
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
