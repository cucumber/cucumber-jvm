package io.cucumber.core.snippets;

import java.util.List;
import java.util.Locale;

final class CamelCaseJoiner implements Joiner {

    @Override
    public String concatenate(List<String> words) {
        StringBuilder functionName = new StringBuilder();
        boolean firstWord = true;
        for (String word : words) {
            if (firstWord) {
                functionName.append(word.toLowerCase(Locale.ROOT));
                firstWord = false;
            } else {
                functionName.append(capitalize(word));
            }
        }
        return functionName.toString();
    }

    private String capitalize(String line) {
        return line.substring(0, 1).toUpperCase(Locale.ROOT) + line.substring(1);
    }

}
