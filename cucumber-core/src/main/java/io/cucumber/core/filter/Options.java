package io.cucumber.core.filter;

import io.cucumber.tagexpressions.Expression;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public interface Options {

    List<Expression> getTagExpressions();

    List<Pattern> getNameFilters();

    Map<URI, Set<Integer>> getLineFilters();

    int getLimitCount();

}
