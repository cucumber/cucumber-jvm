package io.cucumber.core.options;

import io.cucumber.core.filter.Options;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface FilterOptions extends Options {
    List<Pattern> getNameFilters();

    List<String> getTagFilters();

    Map<String, List<Long>> getLineFilters();
}
