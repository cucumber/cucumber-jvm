package io.cucumber.junit.platform.engine;

import io.cucumber.core.runtime.TestCaseResultObserver.Suggestion;
import org.opentest4j.IncompleteExecutionException;

import java.util.Collection;
import java.util.stream.Collectors;

final class UndefinedStepException extends IncompleteExecutionException {

    private static final long serialVersionUID = 1L;

    UndefinedStepException(Collection<Suggestion> suggestions) {
        super(createMessage(suggestions));
        setStackTrace(createSyntheticStacktrace(suggestions));
    }

    private StackTraceElement[] createSyntheticStacktrace(Collection<Suggestion> suggestions) {
        if (suggestions.isEmpty()) {
            return new StackTraceElement[0];
        }
        Suggestion first = suggestions.iterator().next();
        int line = first.getLocation().getLine();
        String uri = first.getUri().toString();
        String stepText = first.getStep();
        StackTraceElement stackTraceElement = new StackTraceElement("✽", stepText, uri, line);
        return new StackTraceElement[] { stackTraceElement };
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
