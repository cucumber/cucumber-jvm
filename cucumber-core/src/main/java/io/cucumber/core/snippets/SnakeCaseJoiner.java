package io.cucumber.core.snippets;

import java.util.List;

class SnakeCaseJoiner implements Joiner {

    @Override
    public String concatenate(List<String> words) {
        StringBuilder functionName = new StringBuilder();
        boolean firstWord = true;
        for (String word : words) {
            if (firstWord) {
                firstWord = false;
            } else {
                functionName.append('_');
            }
            functionName.append(word.toLowerCase());
        }
        return functionName.toString();
    }

}
