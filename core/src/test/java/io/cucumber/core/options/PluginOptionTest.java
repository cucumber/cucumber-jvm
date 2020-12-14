package io.cucumber.core.options;

import io.cucumber.core.plugin.HtmlFormatter;
import io.cucumber.core.plugin.PrettyFormatter;
import io.cucumber.core.plugin.TeamCityPlugin;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PluginOptionTest {

    @Test
    void should_parse_single_plugin_name() {
        PluginOption option = PluginOption.parse("pretty");

        assertAll(
            () -> assertThat(option.pluginClass(), is(PrettyFormatter.class)),
            () -> assertThat(option.argument(), nullValue()),
            () -> assertThat(option.isFormatter(), is(true)),
            () -> assertThat(option.isSummaryPrinter(), is(false)));
    }

    @Test
    void should_parse_argument() {
        PluginOption option = PluginOption.parse("pretty:out.txt");
        assertThat(option.argument(), is("out.txt"));
    }

    @Test
    void should_parse_fully_qualified_class_name() {
        PluginOption option = PluginOption.parse(PrettyFormatter.class.getName());
        assertThat(option.pluginClass(), is(PrettyFormatter.class));
    }

    @Test
    void replaces_incompatible_intellij_plugin() {
        PluginOption option = PluginOption.parse("org.jetbrains.plugins.cucumber.java.run.CucumberJvm5SMFormatter");
        assertThat(option.pluginClass(), is(TeamCityPlugin.class));
    }

    @Test
    void throws_for_known_incompatible_plugins() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> PluginOption.parse("io.qameta.allure.cucumber4jvm.AllureCucumber4Jvm"));

        assertThat(exception.getMessage(),
            is("The plugin specification 'io.qameta.allure.cucumber4jvm.AllureCucumber4Jvm' has a problem:\n" +
                    "\n" +
                    "This plugin is not compatible with this version of Cucumber.\n" +
                    "\n" +
                    "Plugin specifications should have the format of PLUGIN[:[PATH|[URI [OPTIONS]]]\n" +
                    "\n" +
                    "Valid values for PLUGIN are: default_summary, html, json, junit, message, null_summary, pretty, progress, rerun, summary, teamcity, testng, timeline, unused, usage\n"
                    +
                    "\n" +
                    "PLUGIN can also be a fully qualified class name, allowing registration of 3rd party plugins. The 3rd party plugin must implement io.cucumber.plugin.Plugin"));
    }

    @Test
    void throws_for_plugins_that_do_not_implement_plugin() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> PluginOption.parse(String.class.getName()));

        assertThat(exception.getMessage(), is("The plugin specification 'java.lang.String' has a problem:\n" +
                "\n" +
                "'java.lang.String' does not implement 'io.cucumber.plugin.Plugin'.\n" +
                "\n" +
                "Plugin specifications should have the format of PLUGIN[:[PATH|[URI [OPTIONS]]]\n" +
                "\n" +
                "Valid values for PLUGIN are: default_summary, html, json, junit, message, null_summary, pretty, progress, rerun, summary, teamcity, testng, timeline, unused, usage\n"
                +
                "\n" +
                "PLUGIN can also be a fully qualified class name, allowing registration of 3rd party plugins. The 3rd party plugin must implement io.cucumber.plugin.Plugin"));
    }

    @Test
    void throws_for_unknown_plugins() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> PluginOption.parse("no-such-plugin"));

        assertThat(exception.getMessage(), is("The plugin specification 'no-such-plugin' has a problem:\n" +
                "\n" +
                "Could not load plugin class 'no-such-plugin'.\n" +
                "\n" +
                "Plugin specifications should have the format of PLUGIN[:[PATH|[URI [OPTIONS]]]\n" +
                "\n" +
                "Valid values for PLUGIN are: default_summary, html, json, junit, message, null_summary, pretty, progress, rerun, summary, teamcity, testng, timeline, unused, usage\n"
                +
                "\n" +
                "PLUGIN can also be a fully qualified class name, allowing registration of 3rd party plugins. The 3rd party plugin must implement io.cucumber.plugin.Plugin"));
    }

    @Test
    void should_implement_equals_and_hashcode() {
        PluginOption prettyPluginA = PluginOption.forClass(PrettyFormatter.class);
        PluginOption prettyPluginB = PluginOption.forClass(PrettyFormatter.class);
        PluginOption htmlPluginA = PluginOption.forClass(HtmlFormatter.class, "out.html");
        PluginOption htmlPluginB = PluginOption.forClass(HtmlFormatter.class, "out.html");

        assertEquals(prettyPluginA, prettyPluginB);
        assertEquals(prettyPluginA.hashCode(), prettyPluginB.hashCode());
        assertEquals(htmlPluginA, htmlPluginB);
        assertEquals(htmlPluginA.hashCode(), htmlPluginB.hashCode());
        assertNotEquals(prettyPluginA, htmlPluginA);
        assertNotEquals(prettyPluginA.hashCode(), htmlPluginA.hashCode());
    }
}
