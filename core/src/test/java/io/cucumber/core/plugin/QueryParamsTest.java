package io.cucumber.core.plugin;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

class QueryParamsTest {
    @Test
    void it_generates_an_empty_map_for_null() throws UnsupportedEncodingException {
        Map<String, Set<String>> query = QueryParams.parse(null);
        assertThat(query, is(emptyMap()));
    }

    @Test
    void it_escapes_values() throws UnsupportedEncodingException {
        Map<String, Set<String>> query = QueryParams.parse("key=hello+%22world");
        assertThat(query.get("key"), is(equalTo(singleton("hello \"world"))));
    }

    @Test
    void it_parses_params_without_values() throws UnsupportedEncodingException {
        Map<String, Set<String>> query = QueryParams.parse("foo=bar&zap&foo=baz");
        assertThat(query.get("zap"), is(equalTo(emptySet())));
        assertThat(query.get("x"), is(equalTo(null)));
    }

    @Test
    void it_parses_params_specfified_several_times() throws UnsupportedEncodingException {
        Map<String, Set<String>> query = QueryParams.parse("foo=bar&zap&foo=baz");
        assertThat(query.get("foo"), is(equalTo(new HashSet<>(asList("bar", "baz")))));
    }

    @Test
    void it_generates_a_string() throws UnsupportedEncodingException {
        Map<String, Set<String>> query1 = QueryParams.parse("foo=bar&zap&foo=baz");
        String queryString = QueryParams.toString(query1);
        System.out.println("queryString = " + queryString);
        Map<String, Set<String>> query2 = QueryParams.parse(queryString);
        assertThat(query2, is(equalTo(query1)));
    }

}
