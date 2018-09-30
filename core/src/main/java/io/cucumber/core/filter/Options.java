package io.cucumber.core.filter;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface Options {
    List<String> getTagExpressions();

    List<Pattern> getNameFilters();

    Map<String, List<Long>> getLineFilters();
}
