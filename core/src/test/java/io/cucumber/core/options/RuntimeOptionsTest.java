package io.cucumber.core.options;

import io.cucumber.core.plugin.PrettyFormatter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RuntimeOptionsTest {

    private final PluginOption aPlugin = PluginOption.forClass(PrettyFormatter.class);

    @Test
    void shouldRemoveDuplicatePluginRegistrations() {
        RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        runtimeOptions.addSummaryPrinters(Arrays.asList(aPlugin, aPlugin));
        runtimeOptions.addFormatters(Arrays.asList(aPlugin, aPlugin));
        assertThat(runtimeOptions.plugins(), is(singletonList(aPlugin)));
    }

}
