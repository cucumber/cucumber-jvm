package io.cucumber.core.plugin;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QueryParams {
    public static Map<String, Set<String>> parse(String queryString) throws UnsupportedEncodingException {
        Map<String, Set<String>> query = new HashMap<>();
        if(queryString == null) return query;
        for (String pair : queryString.split("&")) {
            int eq = pair.indexOf("=");
            String key;
            String value;
            if (eq < 0) {
                // key with no value
                key = URLDecoder.decode(pair, "utf-8");
                value = null;
            } else {
                // key=value
                key = URLDecoder.decode(pair.substring(0, eq), "utf-8");
                value = URLDecoder.decode(pair.substring(eq + 1), "utf-8");
            }
            Set<String> values = query.computeIfAbsent(key, k -> new HashSet<>());
            if (value != null) {
                values.add(value);
            }
        }
        return query;
    }

    public static String toString(Map<String, Set<String>> query) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Set<String>> pair : query.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            if (pair.getValue().isEmpty()) {
                sb.append(URLEncoder.encode(pair.getKey(), "utf-8"));
            } else {
                for (String value : pair.getValue()) {
                    if (sb.length() > 0) {
                        sb.append('&');
                    }
                    sb.append(URLEncoder.encode(pair.getKey(), "utf-8"));
                    sb.append('=');
                    sb.append(URLEncoder.encode(value, "utf-8"));
                }
            }
        }
        return sb.toString();
    }
}
