package io.cucumber.core.options;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.order.StandardPickleOrders;
import io.cucumber.core.plugin.NoPublishFormatter;
import io.cucumber.core.plugin.PublishFormatter;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.tagexpressions.Expression;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.cucumber.core.resource.ClasspathSupport.rootPackageUri;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public final class RuntimeOptions implements
        io.cucumber.core.feature.Options,
        io.cucumber.core.runner.Options,
        io.cucumber.core.plugin.Options,
        io.cucumber.core.filter.Options,
        io.cucumber.core.backend.Options {

    private final List<URI> glue = new ArrayList<>();
    private final List<Expression> tagExpressions = new ArrayList<>();
    private final List<Pattern> nameFilters = new ArrayList<>();
    private final List<FeatureWithLines> featurePaths = new ArrayList<>();
    private final Set<Plugin> formatters = new LinkedHashSet<>();
    private final Set<Plugin> summaryPrinters = new LinkedHashSet<>();
    private boolean dryRun;
    private boolean monochrome = false;
    private boolean wip = false;
    private SnippetType snippetType = SnippetType.UNDERSCORE;
    private int threads = 1;
    private PickleOrder pickleOrder = StandardPickleOrders.lexicalUriOrder();
    private int count = 0;
    private Class<? extends ObjectFactory> objectFactoryClass;
    private String publishToken;
    private boolean publish;
    private boolean publishQuiet;
    private boolean enablePublishPlugin;

    private RuntimeOptions() {

    }

    public static RuntimeOptions defaultOptions() {
        return new RuntimeOptions();
    }

    void addDefaultFormatterIfAbsent() {
        if (formatters.isEmpty()) {
            formatters.add(PluginOption.parse("progress"));
        }
    }

    void addDefaultSummaryPrinterIfAbsent() {
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

    void addFormatters(List<Plugin> formatters) {
        this.formatters.addAll(formatters);
    }

    void addSummaryPrinters(List<Plugin> summaryPrinters) {
        this.summaryPrinters.addAll(summaryPrinters);
    }

    public boolean isMultiThreaded() {
        return getThreads() > 1;
    }

    public int getThreads() {
        return threads;
    }

    void setThreads(int threads) {
        this.threads = threads;
    }

    @Override
    public List<Plugin> plugins() {
        Set<Plugin> plugins = new LinkedHashSet<>();
        plugins.addAll(formatters);
        plugins.addAll(summaryPrinters);
        plugins.addAll(getPublishPlugin());
        return new ArrayList<>(plugins);
    }

    private List<Plugin> getPublishPlugin() {
        if (!enablePublishPlugin) {
            return emptyList();
        }
        if (publishToken != null) {
            return singletonList(PluginOption.forClass(PublishFormatter.class, publishToken));
        }
        if (publish) {
            return singletonList(PluginOption.forClass(PublishFormatter.class));
        }
        if (publishQuiet) {
            return emptyList();
        }
        return singletonList(PluginOption.forClass(NoPublishFormatter.class));
    }

    @Override
    public boolean isMonochrome() {
        return monochrome;
    }

    public boolean isWip() {
        return wip;
    }

    void setWip(boolean wip) {
        this.wip = wip;
    }

    void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }

    @Override
    public List<URI> getGlue() {
        return unmodifiableList(glue);
    }

    @Override
    public boolean isDryRun() {
        return dryRun;
    }

    @Override
    public SnippetType getSnippetType() {
        return snippetType;
    }

    @Override
    public Class<? extends ObjectFactory> getObjectFactoryClass() {
        return objectFactoryClass;
    }

    void setObjectFactoryClass(Class<? extends ObjectFactory> objectFactoryClass) {
        this.objectFactoryClass = objectFactoryClass;
    }

    void setSnippetType(SnippetType snippetType) {
        this.snippetType = snippetType;
    }

    void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    void setGlue(List<URI> parsedGlue) {
        glue.clear();
        glue.addAll(parsedGlue);
    }

    @Override
    public List<URI> getFeaturePaths() {
        return unmodifiableList(featurePaths.stream()
                .map(FeatureWithLines::uri)
                .sorted()
                .distinct()
                .collect(Collectors.toList()));
    }

    void setFeaturePaths(List<FeatureWithLines> featurePaths) {
        this.featurePaths.clear();
        this.featurePaths.addAll(featurePaths);
    }

    @Override
    public List<Expression> getTagExpressions() {
        return unmodifiableList(tagExpressions);
    }

    @Override
    public List<Pattern> getNameFilters() {
        return unmodifiableList(nameFilters);
    }

    void setNameFilters(List<Pattern> nameFilters) {
        this.nameFilters.clear();
        this.nameFilters.addAll(nameFilters);
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

    public int getCount() {
        return count;
    }

    void setCount(int count) {
        this.count = count;
    }

    void setTagExpressions(List<Expression> tagExpressions) {
        this.tagExpressions.clear();
        this.tagExpressions.addAll(tagExpressions);
    }

    public PickleOrder getPickleOrder() {
        return pickleOrder;
    }

    void setPickleOrder(PickleOrder pickleOrder) {
        this.pickleOrder = pickleOrder;
    }

    void setPublishToken(String token) {
        this.publishToken = token;
    }

    void setPublish(boolean publish) {
        this.publish = publish;
    }

    void setPublishQuiet(boolean publishQuiet) {
        this.publishQuiet = publishQuiet;
    }

    void setEnablePublishPlugin(boolean enablePublishPlugin) {
        this.enablePublishPlugin = enablePublishPlugin;
    }

}
