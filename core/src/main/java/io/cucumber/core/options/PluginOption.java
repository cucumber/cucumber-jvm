package io.cucumber.core.options;

import io.cucumber.core.api.plugin.Plugin;
import io.cucumber.core.api.plugin.SummaryPrinter;
import io.cucumber.core.api.event.ConcurrentEventListener;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.plugin.DefaultSummaryPrinter;
import io.cucumber.core.plugin.HTMLFormatter;
import io.cucumber.core.plugin.JSONFormatter;
import io.cucumber.core.plugin.JUnitFormatter;
import io.cucumber.core.plugin.NullSummaryPrinter;
import io.cucumber.core.plugin.Options;
import io.cucumber.core.plugin.PrettyFormatter;
import io.cucumber.core.plugin.ProgressFormatter;
import io.cucumber.core.plugin.RerunFormatter;
import io.cucumber.core.plugin.TestNGFormatter;
import io.cucumber.core.plugin.TimelineFormatter;
import io.cucumber.core.plugin.UnusedStepsSummaryPrinter;
import io.cucumber.core.plugin.UsageFormatter;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginOption implements Options.Plugin {

    private static final Pattern PLUGIN_WITH_ARGUMENT_PATTERN = Pattern.compile("([^:]+):(.*)");
    private static final HashMap<String, Class<? extends Plugin>> PLUGIN_CLASSES = new HashMap<String, Class<? extends Plugin>>() {{
        put("junit", JUnitFormatter.class);
        put("testng", TestNGFormatter.class);
        put("html", HTMLFormatter.class);
        put("pretty", PrettyFormatter.class);
        put("progress", ProgressFormatter.class);
        put("json", JSONFormatter.class);
        put("usage", UsageFormatter.class);
        put("rerun", RerunFormatter.class);
        put("default_summary", DefaultSummaryPrinter.class);
        put("null_summary", NullSummaryPrinter.class);
        put("unused", UnusedStepsSummaryPrinter.class);
        put("timeline", TimelineFormatter.class);
    }};

    private final String pluginString;
    private final Class<? extends Plugin> pluginClass;
    private final String argument;

    private PluginOption(String pluginString, Class<? extends Plugin> pluginClass, String argument) {
        this.pluginString = pluginString;
        this.pluginClass = pluginClass;
        this.argument = argument;
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
        return EventListener.class.isAssignableFrom(pluginClass) || ConcurrentEventListener.class.isAssignableFrom(pluginClass);
    }

    boolean isSummaryPrinter() {
        return SummaryPrinter.class.isAssignableFrom(pluginClass);
    }

    static PluginOption parse(String pluginArgumentPattern) {
        Matcher pluginWithFile = PLUGIN_WITH_ARGUMENT_PATTERN.matcher(pluginArgumentPattern);
        if (!pluginWithFile.matches()) {
            return new PluginOption(pluginArgumentPattern, parsePluginName(pluginArgumentPattern), null);
        }

        Class<? extends Plugin> pluginClass = parsePluginName(pluginWithFile.group(1));
        return new PluginOption(pluginArgumentPattern, pluginClass, pluginWithFile.group(2));
    }

    private static Class<? extends Plugin> parsePluginName(String pluginName) {
        Class<? extends Plugin> pluginClass = PLUGIN_CLASSES.get(pluginName);
        if (pluginClass == null) {
            pluginClass = loadClass(pluginName);
        }
        return pluginClass;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Plugin> loadClass(String className) {
        try {
            Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(className);

            if (Plugin.class.isAssignableFrom(aClass)) {
                return (Class<? extends Plugin>) aClass;
            }
            throw new CucumberException("Couldn't load plugin class: " + className + ". It does not implement " + Plugin.class.getName());
        } catch (ClassNotFoundException e) {
            throw new CucumberException("Couldn't load plugin class: " + className, e);
        }
    }


}
