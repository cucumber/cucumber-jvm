package io.cucumber.core.options;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.plugin.DefaultSummaryPrinter;
import io.cucumber.core.plugin.HtmlFormatter;
import io.cucumber.core.plugin.JUnitFormatter;
import io.cucumber.core.plugin.JsonFormatter;
import io.cucumber.core.plugin.MessageFormatter;
import io.cucumber.core.plugin.NullSummaryPrinter;
import io.cucumber.core.plugin.Options;
import io.cucumber.core.plugin.PrettyFormatter;
import io.cucumber.core.plugin.ProgressFormatter;
import io.cucumber.core.plugin.RerunFormatter;
import io.cucumber.core.plugin.TeamCityPlugin;
import io.cucumber.core.plugin.TestNGFormatter;
import io.cucumber.core.plugin.TimelineFormatter;
import io.cucumber.core.plugin.UnusedStepsSummaryPrinter;
import io.cucumber.core.plugin.UsageFormatter;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.SummaryPrinter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class PluginOption implements Options.Plugin {

    private static final Logger log = LoggerFactory.getLogger(PluginOption.class);

    private static final Pattern PLUGIN_WITH_ARGUMENT_PATTERN = Pattern.compile("([^:]+):(.*)");
    private static final Map<String, Class<? extends Plugin>> PLUGIN_CLASSES;

    static {
        Map<String, Class<? extends Plugin>> plugins = new HashMap<>();
        plugins.put("default_summary", DefaultSummaryPrinter.class);
        plugins.put("html", HtmlFormatter.class);
        plugins.put("json", JsonFormatter.class);
        plugins.put("junit", JUnitFormatter.class);
        plugins.put("null_summary", NullSummaryPrinter.class);
        plugins.put("pretty", PrettyFormatter.class);
        plugins.put("progress", ProgressFormatter.class);
        plugins.put("message", MessageFormatter.class);
        plugins.put("rerun", RerunFormatter.class);
        plugins.put("summary", DefaultSummaryPrinter.class);
        plugins.put("testng", TestNGFormatter.class);
        plugins.put("timeline", TimelineFormatter.class);
        plugins.put("unused", UnusedStepsSummaryPrinter.class);
        plugins.put("usage", UsageFormatter.class);
        plugins.put("teamcity", TeamCityPlugin.class);
        PLUGIN_CLASSES = unmodifiableMap(plugins);
    }

    private static final Set<String> INCOMPATIBLE_INTELLIJ_IDEA_PLUGIN_CLASSES;

    static {
        Set<String> incompatible = new HashSet<>();
        incompatible.add("org.jetbrains.plugins.cucumber.java.run.CucumberJvmSMFormatter");
        incompatible.add("org.jetbrains.plugins.cucumber.java.run.CucumberJvm2SMFormatter");
        incompatible.add("org.jetbrains.plugins.cucumber.java.run.CucumberJvm3SMFormatter");
        incompatible.add("org.jetbrains.plugins.cucumber.java.run.CucumberJvm4SMFormatter");
        incompatible.add("org.jetbrains.plugins.cucumber.java.run.CucumberJvm5SMFormatter");
        INCOMPATIBLE_INTELLIJ_IDEA_PLUGIN_CLASSES = unmodifiableSet(incompatible);
    }

    private static final Set<String> INCOMPATIBLE_PLUGIN_CLASSES;

    static {
        Set<String> incompatible = new HashSet<>();
        incompatible.add("io.qameta.allure.cucumberjvm.AllureCucumberJvm");
        incompatible.add("io.qameta.allure.cucumber2jvm.AllureCucumber2Jvm");
        incompatible.add("io.qameta.allure.cucumber3jvm.AllureCucumber3Jvm");
        incompatible.add("io.qameta.allure.cucumber4jvm.AllureCucumber4Jvm");
        INCOMPATIBLE_PLUGIN_CLASSES = unmodifiableSet(incompatible);
    }

    private final String pluginString;
    private final Class<? extends Plugin> pluginClass;
    private final String argument;

    private PluginOption(String pluginString, Class<? extends Plugin> pluginClass, String argument) {
        this.pluginString = requireNonNull(pluginString);
        this.pluginClass = requireNonNull(pluginClass);
        this.argument = argument;
    }

    public static PluginOption parse(String pluginSpecification) {
        Matcher pluginWithFile = PLUGIN_WITH_ARGUMENT_PATTERN.matcher(pluginSpecification);
        if (!pluginWithFile.matches()) {
            Class<? extends Plugin> pluginClass = parsePluginName(pluginSpecification, pluginSpecification);
            return new PluginOption(pluginSpecification, pluginClass, null);
        }

        Class<? extends Plugin> pluginClass = parsePluginName(pluginSpecification, pluginWithFile.group(1));
        return new PluginOption(pluginSpecification, pluginClass, pluginWithFile.group(2));
    }

    public static PluginOption forClass(Class<? extends Plugin> pluginClass, String argument) {
        requireNonNull(pluginClass);
        requireNonNull(argument);
        String name = pluginClass.getName();
        return new PluginOption(name + ":" + argument, pluginClass, argument);
    }

    public static PluginOption forClass(Class<? extends Plugin> pluginClass) {
        requireNonNull(pluginClass);
        String name = pluginClass.getName();
        return new PluginOption(name, pluginClass, null);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Plugin> parsePluginName(String pluginSpecification, String pluginName) {
        // Refuse plugins known to implement the old API
        if (INCOMPATIBLE_PLUGIN_CLASSES.contains(pluginName)) {
            throw createPluginIsNotCompatible(pluginSpecification);
        }

        // Replace IDEA plugin with TeamCity
        if (INCOMPATIBLE_INTELLIJ_IDEA_PLUGIN_CLASSES.contains(pluginName)) {
            log.debug(() -> "Incompatible IntelliJ IDEA Plugin detected. Falling back to teamcity plugin");
            return TeamCityPlugin.class;
        }

        if (PLUGIN_CLASSES.containsKey(pluginName)) {
            return PLUGIN_CLASSES.get(pluginName);
        }

        try {
            Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(pluginName);
            if (Plugin.class.isAssignableFrom(aClass)) {
                return (Class<? extends Plugin>) aClass;
            }
            throw createClassDoesNotImplementPlugin(pluginSpecification, aClass);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            throw createCouldNotLoadClass(pluginSpecification, pluginName, e);
        }
    }

    private static IllegalArgumentException createPluginIsNotCompatible(String pluginSpecification) {
        return new IllegalArgumentException(invalidPluginMessage(pluginSpecification,
            "This plugin is not compatible with this version of Cucumber"));
    }

    private static IllegalArgumentException createClassDoesNotImplementPlugin(
            String pluginSpecification,
            Class<?> pluginClass
    ) {
        return new IllegalArgumentException(invalidPluginMessage(pluginSpecification,
            "'" + pluginClass.getName() + "' does not implement '" + Plugin.class.getName() + "'"));
    }

    private static IllegalArgumentException createCouldNotLoadClass(
            String pluginSpecification, String className,
            Throwable e
    ) {
        return new IllegalArgumentException(
            invalidPluginMessage(pluginSpecification, "Could not load plugin class '" + className + "'"), e);
    }

    private static String invalidPluginMessage(String pluginSpecification, String problem) {
        return "The plugin specification '" + pluginSpecification + "' has a problem:\n" +
                "\n" +
                problem + ".\n" +
                "\n" +
                "Plugin specifications should have the format of PLUGIN[:[PATH|[URI [OPTIONS]]]\n" +
                "\n" +
                "Valid values for PLUGIN are: " + PLUGIN_CLASSES.keySet().stream().sorted()
                        .collect(joining(", "))
                + "\n" +
                "\n" +
                "PLUGIN can also be a fully qualified class name, allowing registration of 3rd party plugins. " +
                "The 3rd party plugin must implement " + Plugin.class.getName();
    }

    @Override
    public Class<? extends Plugin> pluginClass() {
        return pluginClass;
    }

    @Override
    public String argument() {
        return argument;
    }

    @Override
    public String pluginString() {
        return pluginString;
    }

    boolean isFormatter() {
        return EventListener.class.isAssignableFrom(pluginClass)
                || ConcurrentEventListener.class.isAssignableFrom(pluginClass);
    }

    boolean isSummaryPrinter() {
        return SummaryPrinter.class.isAssignableFrom(pluginClass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PluginOption that = (PluginOption) o;
        return pluginClass.equals(that.pluginClass) && Objects.equals(argument, that.argument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginClass, argument);
    }
}
