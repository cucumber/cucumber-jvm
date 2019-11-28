package io.cucumber.core.gherkin8;

import io.cucumber.core.gherkin.DocStringArgument;
import io.cucumber.messages.Messages.PickleStepArgument.PickleDocString;

final class Gherkin8DocStringArgument implements DocStringArgument {

    private final PickleDocString docString;
    private final int line;

    Gherkin8DocStringArgument(PickleDocString docString, int line) {
        this.docString = docString;
        this.line = line;
    }

    @Override
    public String getContent() {
        return docString.getContent();
    }

    @Override
    public String getContentType() {
        return docString.getContentType();
    }

    @Override
    public int getLine() {
        return line;
    }
}
