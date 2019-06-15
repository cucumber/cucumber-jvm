package io.cucumber.core.filter;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public interface Options {
    List<String> getTagExpressions();

    List<Pattern> getNameFilters();

    Map<URI, Set<Integer>> getLineFilters();

    int getLimitCount();
}
