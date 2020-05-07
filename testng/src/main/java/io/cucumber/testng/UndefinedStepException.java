package io.cucumber.testng;

import io.cucumber.core.runtime.TestCaseResultObserver.Suggestion;
import org.testng.SkipException;

import java.util.List;

import static java.util.stream.Collectors.joining;

final class UndefinedStepException extends SkipException {

    private static final long serialVersionUID = 1L;

    UndefinedStepException(List<Suggestion> suggestions) {
        super(createMessage(suggestions));
    }

    private static String createMessage(List<Suggestion> suggestions) {
        return suggestions.stream()
                .map(suggestion -> createStepMessage(suggestion.getStep(), suggestion.getSnippets()))
                .collect(joining("\n", createPreAmble(suggestions), ""));
    }

    private static String createStepMessage(String stepText, List<String> snippets) {
        StringBuilder sb = new StringBuilder("The step \"" + stepText + "\" is undefined");
        appendSnippets(snippets, sb);
        return sb.toString();
    }

    private static String createPreAmble(List<Suggestion> suggestions) {
        return suggestions.size() < 2 ? "" : "There were " + suggestions.size() + " undefined steps\n";
    }

    private static void appendSnippets(List<String> snippets, StringBuilder sb) {

        if (snippets.isEmpty()) {
            return;
        }
        sb.append(". You can implement it using the snippet(s) below:\n\n");
        sb.append(snippets.stream().collect(joining("\n---\n", "", "\n")));
    }

    @Override
    public boolean isSkip() {
        return false;
    }

}
