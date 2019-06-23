package io.cucumber.core.snippets;

class UnderscoreJoiner implements Joiner {
    @Override
    public String concatenate(String[] words) {
        StringBuilder functionName = new StringBuilder();
        boolean firstWord = true;
        for (String word : words) {
            if (firstWord) {
                word = word.toLowerCase();
            } else {
                functionName.append('_');
            }
            functionName.append(word);
            firstWord = false;
        }
        return functionName.toString();
    }
}
