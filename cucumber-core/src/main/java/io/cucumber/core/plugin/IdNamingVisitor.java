package io.cucumber.core.plugin;

import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.Rule;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.TableRow;
import io.cucumber.query.LineageReducer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

class IdNamingVisitor implements LineageReducer.Collector<String> {

    private static final Pattern replacementPattern = Pattern.compile("[\\s'_,!]");

    private final List<String> parts = new ArrayList<>();

    @Override
    public void add(Feature feature) {
        parts.add(formatId(feature.getName()));
    }

    @Override
    public void add(Rule rule) {
        parts.add(formatId(rule.getName()));
    }

    @Override
    public void add(Scenario scenario) {
        parts.add(formatId(scenario.getName()));
    }

    @Override
    public void add(Examples examples, int index) {
        parts.add(formatId(examples.getName()));
    }

    @Override
    public void add(TableRow example, int index) {
        // json report uses base-1 indexing, and skips the first row
        parts.add(String.valueOf(index + 2));
    }

    @Override
    public void add(Pickle pickle) {
        formatId(pickle.getName());
    }

    @Override
    public String finish() {
        return String.join(";", parts);
    }

    static String formatId(String name) {
        return replacementPattern.matcher(name).replaceAll("-").toLowerCase(Locale.ROOT);
    }
}
