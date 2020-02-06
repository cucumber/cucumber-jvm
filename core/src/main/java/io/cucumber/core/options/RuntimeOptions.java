package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.StandardPickleOrders;
import io.cucumber.core.snippets.SnippetType;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.cucumber.core.resource.ClasspathSupport.rootPackageUri;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public final class RuntimeOptions implements
    io.cucumber.core.feature.Options,
    io.cucumber.core.runner.Options,
    io.cucumber.core.plugin.Options,
    io.cucumber.core.filter.Options,
    io.cucumber.core.backend.Options {

    private final List<URI> glue = new ArrayList<>();
    private final List<String> tagExpressions = new ArrayList<>();
    private final List<Pattern> nameFilters = new ArrayList<>();
    private final List<FeatureWithLines> featurePaths = new ArrayList<>();

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
    private Class<? extends ObjectFactory> objectFactoryClass;

    private RuntimeOptions() {

    }

    public static RuntimeOptions defaultOptions() {
        return new RuntimeOptions();
    }

    void addDefaultFormatterIfAbsent(){
        if (formatters.isEmpty()) {
            formatters.add(PluginOption.parse("progress"));
        }
    }
    void addDefaultSummaryPrinterIfAbsent(){
        if (summaryPrinters.isEmpty()) {
            summaryPrinters.add(PluginOption.parse("default_summary"));
        }
    }

    void addDefaultGlueIfAbsent() {
        if (glue.isEmpty()) {
            glue.add(rootPackageUri());
        }
    }

    void addDefaultFeaturePathIfAbsent() {
        if (featurePaths.isEmpty()) {
            featurePaths.add(FeatureWithLines.create(rootPackageUri(), emptyList()));
        }
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
        return unmodifiableList(featurePaths.stream()
            .map(FeatureWithLines::uri)
            .sorted()
            .distinct()
            .collect(Collectors.toList()));
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

    void setFeaturePaths(List<FeatureWithLines> featurePaths) {
        this.featurePaths.clear();
        this.featurePaths.addAll(featurePaths);
    }

    void setGlue(List<URI> parsedGlue) {
        glue.clear();
        glue.addAll(parsedGlue);
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
        Map<URI, Set<Integer>> lineFilters = new HashMap<>();
        featurePaths.forEach(featureWithLines -> {
            SortedSet<Integer> lines = featureWithLines.lines();
            URI uri = featureWithLines.uri();
            if (lines.isEmpty()) {
                return;
            }
            lineFilters.putIfAbsent(uri, new TreeSet<>());
            lineFilters.get(uri).addAll(lines);
        });
        return unmodifiableMap(lineFilters);
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

    @Override
    public Class<? extends ObjectFactory> getObjectFactoryClass() {
        return objectFactoryClass;
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

    void setObjectFactoryClass(Class<? extends ObjectFactory> objectFactoryClass) {
        this.objectFactoryClass = objectFactoryClass;
    }
}
