package io.cucumber.junit;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

final class UndefinedStepException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    UndefinedStepException(Collection<String> snippets) {
        super(createMessage(snippets), null, false, false);
    }

    private static String createMessage(Collection<String> snippets) {
        StringBuilder sb = new StringBuilder("This step is undefined");
        appendSnippets(snippets, sb);
        return sb.toString();
    }

    private static void appendSnippets(Collection<String> snippets, StringBuilder sb) {
        if (snippets.isEmpty()) {
            return;
        }
        sb.append(". You can implement it using the snippet(s) below:\n\n");
        snippets.forEach(snippet -> {
            sb.append(snippet);
            sb.append("\n");
        });
    }

    UndefinedStepException(String stepText, Collection<String> snippets, Collection<Collection<String>> otherSnippets) {
        super(createMessage(stepText, snippets, otherSnippets), null, false, false);
    }

    private static String createMessage(
            String stepText, Collection<String> snippets, Collection<Collection<String>> otherSnippets
    ) {
        Set<String> otherUniqueSnippets = otherSnippets.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        otherUniqueSnippets.removeAll(snippets);

        StringBuilder sb = new StringBuilder("The step \"" + stepText + "\" is undefined");
        appendSnippets(snippets, sb);
        appendOtherSnippets(otherUniqueSnippets, sb);
        return sb.toString();
    }

    private static void appendOtherSnippets(Collection<String> otherSnippets, StringBuilder sb) {
        if (otherSnippets.isEmpty()) {
            return;
        }
        sb.append("\n");
        sb.append("\n");
        sb.append("Some other steps were also undefined:\n\n");
        otherSnippets.forEach(snippet -> {
            sb.append(snippet);
            sb.append("\n");
        });
    }

}
