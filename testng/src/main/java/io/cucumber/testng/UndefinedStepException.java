package io.cucumber.testng;

import org.testng.SkipException;

import java.util.List;

import static java.util.stream.Collectors.joining;

final class UndefinedStepException extends SkipException {
    private static final long serialVersionUID = 1L;
    private final boolean strict;

    UndefinedStepException(List<Suggestion> suggestions, boolean strict) {
        super(createMessage(suggestions));
        this.strict = strict;
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
        sb.append(". You can implement it using the snippet(s) below:\n\n");
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

    @Override
    public boolean isSkip() {
        return !strict;
    }
}
