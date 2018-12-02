package io.cucumber.core.options;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface FilterOptions {
    List<Pattern> getNameFilters();

    List<String> getTagFilters();

    Map<String, List<Long>> getLineFilters();
}
