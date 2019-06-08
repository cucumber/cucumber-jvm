package io.cucumber.core.options;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public interface FilterOptions {
    List<Pattern> getNameFilters();

    List<String> getTagFilters();

    Map<URI, Set<Integer>> getLineFilters();
    
    int getLimitCount();
}
