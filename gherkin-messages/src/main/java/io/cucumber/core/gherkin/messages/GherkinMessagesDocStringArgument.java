package io.cucumber.core.gherkin.messages;

import io.cucumber.core.gherkin.DocStringArgument;
import io.cucumber.messages.Messages.PickleStepArgument.PickleDocString;

final class GherkinMessagesDocStringArgument implements DocStringArgument {

    private final PickleDocString docString;
    private final int line;

    GherkinMessagesDocStringArgument(PickleDocString docString, int line) {
        this.docString = docString;
        this.line = line;
    }

    @Override
    public String getContent() {
        return docString.getContent();
    }

    @Override
    public String getContentType() {
        return docString.getMediaType();
    }

    @Override
    public String getMediaType() {
        return docString.getMediaType();
    }

    @Override
    public int getLine() {
        return line;
    }
}
