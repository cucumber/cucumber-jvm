package io.cucumber.junit;

import io.cucumber.plugin.event.SnippetsSuggestedEvent.Suggestion;

import java.util.Collection;
import java.util.stream.Collectors;

final class UndefinedStepException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    UndefinedStepException(Collection<Suggestion> suggestions) {
        super(createMessage(suggestions), null, false, false);
    }

    private static String createMessage(Collection<Suggestion> suggestions) {
        if (suggestions.isEmpty()) {
            return "This step is undefined";
        }
        Suggestion first = suggestions.iterator().next();
        StringBuilder sb = new StringBuilder("The step '" + first.getStep() + "'");
        if (suggestions.size() == 1) {
            sb.append(" is undefined.");
        } else {
            sb.append(" and ").append(suggestions.size() - 1).append(" other step(s) are undefined.");
        }
        sb.append("\n");
        if (suggestions.size() == 1) {
            sb.append("You can implement this step using the snippet(s) below:\n\n");
        } else {
            sb.append("You can implement these steps using the snippet(s) below:\n\n");
        }
        String snippets = suggestions
                .stream()
                .map(Suggestion::getSnippets)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.joining("\n", "", "\n"));
        sb.append(snippets);
        return sb.toString();
    }
}
