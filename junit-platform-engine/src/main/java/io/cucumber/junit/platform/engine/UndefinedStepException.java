package io.cucumber.junit.platform.engine;

import org.opentest4j.IncompleteExecutionException;

import java.util.List;

import static java.util.stream.Collectors.joining;

final class UndefinedStepException extends IncompleteExecutionException {
    private static final long serialVersionUID = 1L;

    UndefinedStepException(List<Suggestion> suggestions) {
        super(createMessage(suggestions));
    }

    private static String createMessage(List<Suggestion> suggestions) {
        return suggestions.stream()
            .map(suggestion -> createStepMessage(suggestion.step, suggestion.snippets))
            .collect(joining("\n", createPreAmble(suggestions), ""));
    }

    private static String createPreAmble(List<Suggestion> suggestions) {
        return suggestions.size() < 2 ? "" : "There were " + suggestions.size() + " undefined steps\n";
    }

    private static String createStepMessage(String stepText, List<String> snippets) {
        StringBuilder sb = new StringBuilder("The step \"" + stepText + "\" is undefined");
        appendSnippets(snippets, sb);
        return sb.toString();
    }

    private static void appendSnippets(List<String> snippets, StringBuilder sb) {

        if (snippets.isEmpty()) {
            return;
        }
        sb.append(". You can implement it using tne snippet(s) below:\n\n");
        sb.append(snippets.stream().collect(joining("\n---\n", "", "\n")));
    }

    static final class Suggestion {
        final String step;
        final List<String> snippets;

        Suggestion(String step, List<String> snippets) {
            this.step = step;
            this.snippets = snippets;
        }
    }
}
