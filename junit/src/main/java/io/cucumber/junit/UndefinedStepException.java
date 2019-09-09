package io.cucumber.junit;

import java.util.Collection;
import java.util.List;

final class UndefinedStepException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    UndefinedStepException(List<String> snippets) {
        super(createMessage(snippets), null, false, false);
    }

    private static String createMessage(List<String> snippets) {
        StringBuilder sb = new StringBuilder("This step is undefined");
        appendSnippets(snippets, sb);
        return sb.toString();
    }

    UndefinedStepException(String stepText, List<String> snippets, Collection<List<String>> otherSnippets) {
        super(createMessage(stepText, snippets, otherSnippets), null, false, false);
    }

    private static String createMessage(String stepText, List<String> snippets, Collection<List<String>> otherSnippets) {
        StringBuilder sb = new StringBuilder("The step \"" + stepText + "\" is undefined");
        appendSnippets(snippets, sb);
        appendOtherSnippets(otherSnippets, sb);
        return sb.toString();
    }

    private static void appendOtherSnippets(Collection<List<String>> otherSnippets, StringBuilder sb) {
        if (otherSnippets.isEmpty()) {
            return;
        }

        sb.append("\n");
        sb.append("\n");
        sb.append("Some other steps were also undefined:\n\n");
        otherSnippets.forEach(snippet -> {
            sb.append(String.join("\n", snippet));
            sb.append("\n");
        });
    }

    private static void appendSnippets(List<String> snippets, StringBuilder sb) {
        if (snippets.isEmpty()) {
            return;
        }
        sb.append(". You can implement it using tne snippet(s) below:\n\n");
        sb.append(String.join("\n", snippets));
    }

}
